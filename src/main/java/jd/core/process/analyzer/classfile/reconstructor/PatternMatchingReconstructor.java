/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jd.core.process.analyzer.classfile.reconstructor;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.CodeException;

import jd.core.model.classfile.ClassFile;
import jd.core.process.analyzer.instruction.bytecode.util.ByteCodeUtil;
import jd.core.process.analyzer.instruction.fast.visitor.CheckLocalVariableUsedVisitor;
import jd.core.process.deserializer.ClassFileDeserializer;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.AStore;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ExceptionLoad;
import jd.core.model.instruction.bytecode.instruction.Goto;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.LoadInstruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.model.instruction.bytecode.instruction.InstanceOf;
import jd.core.model.instruction.bytecode.instruction.Instruction;

/**
 * Reconstructs the pattern matching of 'instanceof' compiled by javac:
 *
 * <pre>
 * if (o instanceof String) goto L; s = (String) o; ...
 * </pre>
 *
 * The store of the binding variable is removed from the instructions, so that the
 * conditions around it can be combined again, and the variable is attached to the
 * 'instanceof' operator.
 */
// Linear matching of a fixed bytecode shape
@SuppressWarnings("java:S3776")
public final class PatternMatchingReconstructor {

    private PatternMatchingReconstructor() {
    }

    public static void reconstruct(ClassFile classFile, Method method, List<Instruction> list) {
        LocalVariables localVariables = method.getLocalVariables();
        if (localVariables == null) {
            return;
        }
        boolean recordPattern = false;
        for (int i = list.size() - 2; i >= 0; i--) {
            if (list.get(i) instanceof IfInstruction test && test.getValue() instanceof InstanceOf instanceOf
                    && instanceOf.getPatternVariable() == null && instanceOf.getRecordPatternVariables() == null) {
                recordPattern |= reconstructRecordPattern(classFile, method, list, i, instanceOf);
            }
        }
        if (recordPattern) {
            removeMatchExceptionHandlers(classFile, method, list);
        }
        for (int i = list.size() - 2; i >= 0; i--) {
            if (list.get(i) instanceof IfInstruction test && test.getValue() instanceof InstanceOf instanceOf
                    && test.getCmp() == ByteCodeConstants.CMP_EQ && instanceOf.getPatternVariable() == null
                    && list.get(i + 1) instanceof AStore store && isBinding(instanceOf, store)
                    && test.getJumpOffset() > store.getOffset()
                    && !readsRecordComponents(classFile, list, i + 2, store.getIndex())) {
                LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(store.getIndex(), store.getOffset());
                if (lv != null && declaredBy(method, lv, store) && !isStoredElsewhere(method, list, store, lv)) {
                    instanceOf.setPatternVariable(lv);
                    markBinding(classFile, method, lv);
                    list.remove(i + 1);
                }
            }
        }
    }

    /**
     * if (o instanceof R) goto END; r = o; c1 = r.a(); t1 = c1; goto END if true; x = c1; ...
     */
    private static boolean reconstructRecordPattern(ClassFile classFile, Method method,
            List<Instruction> list, int ifIndex, InstanceOf instanceOf) {
        if (!(instanceOf.getObjectref() instanceof ALoad tested) || ifIndex + 2 >= list.size()
                || !(list.get(ifIndex + 1) instanceof AStore recordStore) || !(unwrapCast(recordStore.getValueref()) instanceof ALoad source)
                || source.getIndex() != tested.getIndex()) {
            return false;
        }
        List<LocalVariable> bindings = new ArrayList<>();
        int end = collectComponents(classFile, method, list, ifIndex + 2, recordStore.getIndex(),
                classFile.getConstantPool().getConstantClassName(instanceOf.getIndex()), bindings);
        int bodyEnd = ((IfInstruction) list.get(ifIndex)).getJumpOffset();
        if (end < 0 || isUsedAfter(list, end, recordStore.getIndex(), bodyEnd)
                || areTemporariesUsedAfter(list, end, method, bodyEnd)) {
            // The record itself is still needed: keep the ordinary type pattern
            return false;
        }
        list.subList(ifIndex + 1, end).clear();
        for (LocalVariable lv : bindings) {
            markBinding(classFile, method, lv);
        }
        instanceOf.setRecordPatternVariables(bindings);
        return true;
    }

    /**
     * Collects the bindings of the components of a record pattern, starting at {@code start}:
     * {@code c1 = r.a(); t1 = c1; [goto END if true]; x = c1; c2 = r.b(); ...}.
     *
     * @return index of the first instruction after the components, or -1 if there are none
     */
    static int collectComponents(ClassFile classFile, Method method, List<Instruction> list,
            int start, int recordVariable, String recordName, List<LocalVariable> bindings) {
        LocalVariables localVariables = method.getLocalVariables();
        ConstantPool cp = classFile.getConstantPool();
        if (!isRecord(classFile, recordName)) {
            return -1;
        }
        List<String> accessedNames = new ArrayList<>();
        List<Integer> accessOffsets = new ArrayList<>();
        method.getRecordTemporaries().clear();
        int end = start;
        while (end < list.size() && list.get(end) instanceof StoreInstruction component
                && component.getValueref() instanceof InvokeNoStaticInstruction accessor
                && accessor.getArgs().isEmpty()
                && accessor.getObjectref() instanceof ALoad owner && owner.getIndex() == recordVariable
                && recordName.equals(cp.getConstantClassName(cp.getConstantMethodref(accessor.getIndex()).getClassIndex()))) {
            accessOffsets.add(accessor.getOffset());
            accessedNames.add(cp.getConstantUtf8(cp.getConstantNameAndType(
                    cp.getConstantMethodref(accessor.getIndex()).getNameAndTypeIndex()).getNameIndex()));
            int next = end + 1;
            StoreInstruction binding = null;
            method.getRecordTemporaries().add(localVariables.getLocalVariableWithIndexAndOffset(component.getIndex(), component.getOffset()));
            while (next < list.size()) {
                Instruction instruction = list.get(next);
                if (instruction instanceof StoreInstruction copy && copy.getValueref() instanceof LoadInstruction load
                        && load.getIndex() == component.getIndex()) {
                    if (binding != null) {
                        // An earlier copy is a temporary of the pattern
                        method.getRecordTemporaries().add(localVariables.getLocalVariableWithIndexAndOffset(binding.getIndex(), binding.getOffset()));
                    }
                    binding = copy;
                } else if (!(instruction instanceof IfInstruction check && check.getValue() instanceof IConst)) {
                    break;
                }
                next++;
            }
            if (binding == null) {
                return -1;
            }
            LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(binding.getIndex(), binding.getOffset());
            if (lv == null || !declaredBy(method, lv, binding)) {
                return -1;
            }
            bindings.add(lv);
            end = next;
        }
        if (bindings.isEmpty() || !accessedNames.equals(componentNames(classFile, recordName))
                || !isWrappedByMatchException(classFile, method, list, accessOffsets)) {
            // A record pattern reads every component, in the order of the declaration
            bindings.clear();
            return -1;
        }
        method.getRecordAccessOffsets().addAll(accessOffsets);
        return end;
    }

    /** A variable also assigned elsewhere is shared by several branches: it is not a pattern variable. */
    private static boolean isStoredElsewhere(Method method, List<Instruction> list, StoreInstruction store, LocalVariable lv) {
        LocalVariables localVariables = method.getLocalVariables();
        for (Instruction instruction : list) {
            if (CheckLocalVariableUsedVisitor.visit(other -> {
                if (!(other instanceof StoreInstruction s) || s == store || s.getIndex() != store.getIndex()) {
                    return false;
                }
                LocalVariable stored = localVariables.getLocalVariableWithIndexAndOffset(s.getIndex(), s.getOffset());
                return stored != null && stored.getNameIndex() == lv.getNameIndex();
            }, instruction)) {
                return true;
            }
        }
        return false;
    }

    /** Whether the store is the one declaring the variable, not an assignment to a variable that existed before. */
    static boolean declaredBy(Method method, LocalVariable lv, StoreInstruction store) {
        byte[] code = method.getCode();
        int next = code == null ? store.getOffset() : ByteCodeUtil.nextInstructionOffset(code, store.getOffset());
        return lv.getStartPc() == store.getOffset() || lv.getStartPc() == next;
    }

    /** Whether the temporaries holding the components are read after the pattern: they are variables of the user then. */
    static boolean areTemporariesUsedAfter(List<Instruction> list, int from, Method method, int endOffset) {
        java.util.Set<LocalVariable> temporaries = method.getRecordTemporaries();
        for (int i = from; i < list.size() && list.get(i).getOffset() < endOffset; i++) {
            if (CheckLocalVariableUsedVisitor.visit(
                    instruction -> instruction instanceof LoadInstruction && !(instruction instanceof StoreInstruction)
                            && temporaries.contains(method.getLocalVariables()
                                    .getLocalVariableWithIndexAndOffset(instruction.getIndex(), instruction.getOffset())),
                    list.get(i))) {
                return true;
            }
        }
        return false;
    }

    /** Whether a local variable is read by the instructions from an index on, before an offset. */
    static boolean isUsedAfter(List<Instruction> list, int from, int variableIndex, int endOffset) {
        for (int i = from; i < list.size() && list.get(i).getOffset() < endOffset; i++) {
            if (CheckLocalVariableUsedVisitor.visit(
                    instruction -> instruction instanceof LoadInstruction && !(instruction instanceof StoreInstruction)
                            && instruction.getIndex() == variableIndex, list.get(i))) {
                return true;
            }
        }
        return false;
    }

    /** Names of the components of a record: its instance fields, in the order of the declaration. */
    private static List<String> componentNames(ClassFile classFile, String recordName) {
        ClassFile root = classFile;
        while (root.getOuterClass() != null) {
            root = root.getOuterClass();
        }
        ClassFile recordClass = findClass(root, recordName);
        if (recordClass == null && classFile.getLoader() != null) {
            recordClass = ClassFileDeserializer.deserialize(classFile.getLoader(), recordName + ".class", true);
        }
        List<String> names = new ArrayList<>();
        if (recordClass != null && recordClass.getRecordComponents() != null) {
            // The analysis of the recordClass already turned its fields into components
            for (jd.core.model.classfile.RecordComponent component : recordClass.getRecordComponents()) {
                names.add(component.name());
            }
        } else if (recordClass != null) {
            for (jd.core.model.classfile.Field field : recordClass.getFields()) {
                if ((field.getAccessFlags() & Const.ACC_STATIC) == 0) {
                    names.add(recordClass.getConstantPool().getConstantUtf8(field.getNameIndex()));
                }
            }
        }
        return names;
    }

    /** Removes the handler throwing MatchException that javac generates around record patterns. */
    private static ClassFile findClass(ClassFile candidate, String internalName) {
        if (candidate.getThisClassName().equals(internalName)) {
            return candidate;
        }
        if (candidate.getInnerClassFiles() != null) {
            for (ClassFile inner : candidate.getInnerClassFiles()) {
                ClassFile found = findClass(inner, internalName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static boolean isRecord(ClassFile classFile, String internalName) {
        ClassFile root = classFile;
        while (root.getOuterClass() != null) {
            root = root.getOuterClass();
        }
        ClassFile known = findClass(root, internalName);
        if (known != null) {
            return known.isRecord();
        }
        var typeTypes = new org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker(classFile.getLoader())
                .makeTypeTypes(internalName);
        return typeTypes != null && typeTypes.getSuperType() != null
                && "java/lang/Record".equals(typeTypes.getSuperType().getInternalName());
    }

    /** A handler written by the user may also throw a MatchException: all its ranges must be record accesses. */
    private static boolean protectsRecordAccess(CodeException[] exceptions, int handlerPC, java.util.Set<Integer> accessOffsets) {
        for (CodeException candidate : exceptions) {
            if (candidate.getHandlerPC() == handlerPC
                    && accessOffsets.stream().noneMatch(offset -> candidate.getStartPC() <= offset && offset < candidate.getEndPC())) {
                return false;
            }
        }
        return true;
    }

    /** Whether the instructions at an index are the handler rethrowing a MatchException that the compiler adds. */
    private static boolean isMatchExceptionHandler(ClassFile classFile, List<Instruction> list, int index) {
        ConstantPool cp = classFile.getConstantPool();
        return index >= 0 && index + 1 < list.size() && list.get(index) instanceof AStore store
                && store.getValueref() instanceof ExceptionLoad
                && list.get(index + 1) instanceof AThrow thrown && thrown.getValueref() instanceof InvokeNew created
                && "java/lang/MatchException".equals(cp.getConstantClassName(cp.getConstantMethodref(created.getIndex()).getClassIndex()));
    }

    /** Record patterns wrap the exceptions of the accessors: an ordinary call has no such handler around it. */
    private static boolean isWrappedByMatchException(ClassFile classFile, Method method, List<Instruction> list, List<Integer> accessOffsets) {
        CodeException[] exceptions = method.getCodeExceptions();
        if (exceptions == null) {
            return false;
        }
        for (int offset : accessOffsets) {
            boolean wrapped = false;
            for (CodeException exception : exceptions) {
                if (exception.getStartPC() <= offset && offset < exception.getEndPC()) {
                    for (int i = 0; i < list.size() && !wrapped; i++) {
                        wrapped = list.get(i).getOffset() == exception.getHandlerPC() && isMatchExceptionHandler(classFile, list, i);
                    }
                }
            }
            if (!wrapped) {
                return false;
            }
        }
        return true;
    }

    static void removeMatchExceptionHandlers(ClassFile classFile, Method method, List<Instruction> list) {
        CodeException[] exceptions = method.getCodeExceptions();
        if (exceptions == null) {
            return;
        }
        List<CodeException> remaining = new ArrayList<>(List.of(exceptions));
        for (CodeException exception : exceptions) {
            int handler = -1;
            for (int i = 0; i < list.size() - 1; i++) {
                if (list.get(i).getOffset() == exception.getHandlerPC()) {
                    handler = i;
                    break;
                }
            }
            if (handler > 0 && isMatchExceptionHandler(classFile, list, handler)
                    && list.get(handler + 1) instanceof AThrow thrown) {
                if (!protectsRecordAccess(exceptions, exception.getHandlerPC(), method.getRecordAccessOffsets())) {
                    continue;
                }
                remaining.removeIf(candidate -> candidate.getHandlerPC() == exception.getHandlerPC());
                int from = handler;
                if (list.get(handler - 1) instanceof Goto jump && jump.getJumpOffset() > thrown.getOffset()) {
                    from--;
                }
                list.subList(from, handler + 2).clear();
            }
        }
        method.setCodeExceptions(remaining.toArray(new CodeException[0]));
    }

    private static Instruction unwrapCast(Instruction instruction) {
        return instruction instanceof CheckCast cast ? cast.getObjectref() : instruction;
    }

    private static boolean isStoreOpcode(int opcode) {
        return opcode >= Const.ISTORE && opcode <= Const.ASTORE;
    }

    /** A range of the same variable starts after a jump, not after the store of a new variable. */
    private static boolean isDeclaredByStore(byte[] code, LocalVariable lv) {
        int start = lv.getStartPc();
        if (code == null || start < 1 || start > code.length) {
            return false;
        }
        int previous = code[start - 1] & 255;
        boolean shortStore = previous >= Const.ISTORE_0 && previous <= Const.ASTORE_3 && (previous - Const.ISTORE_0) % 4 == lv.getIndex();
        boolean longStore = start >= 2 && isStoreOpcode(code[start - 2] & 255) && previous == lv.getIndex();
        // The range of a variable may have been extended to its first store
        int current = start < code.length ? code[start] & 255 : 0;
        boolean storeAtStart = current >= Const.ISTORE_0 && current <= Const.ASTORE_3 && (current - Const.ISTORE_0) % 4 == lv.getIndex()
                || isStoreOpcode(current) && start + 1 < code.length && (code[start + 1] & 255) == lv.getIndex();
        return shortStore || longStore || storeAtStart;
    }

    /**
     * Declares a variable by its pattern. The debug information of a variable that stays in scope
     * after the statement binding it has one range for each part of the code using it, which are
     * all the same variable for the source code.
     */
    static void markBinding(ClassFile classFile, Method method, LocalVariable binding) {
        LocalVariables localVariables = method.getLocalVariables();
        ConstantPool cp = classFile.getConstantPool();
        String name = cp.getConstantUtf8(binding.getNameIndex());
        for (int i = 0; i < localVariables.size(); i++) {
            LocalVariable lv = localVariables.getLocalVariableAt(i);
            if (lv == binding || lv.getIndex() == binding.getIndex() && lv.getSignatureIndex() == binding.getSignatureIndex()
                    && name.equals(cp.getConstantUtf8(lv.getNameIndex())) && !isDeclaredByStore(method.getCode(), lv)) {
                lv.setDeclarationFlag(true);
                lv.setPatternBinding(true);
            }
        }
    }

    /** A nested record pattern reads the components of the cast value: it is not a plain type pattern. */
    private static boolean readsRecordComponents(ClassFile classFile, List<Instruction> list, int index, int variable) {
        if (index >= list.size() || !(list.get(index) instanceof StoreInstruction component)
                || !(component.getValueref() instanceof InvokeNoStaticInstruction accessor)
                || !accessor.getArgs().isEmpty() || !(accessor.getObjectref() instanceof ALoad owner)
                || owner.getIndex() != variable) {
            return false;
        }
        ConstantPool cp = classFile.getConstantPool();
        return isRecord(classFile, cp.getConstantClassName(cp.getConstantMethodref(accessor.getIndex()).getClassIndex()));
    }

    private static boolean isBinding(InstanceOf instanceOf, AStore store) {
        return store.getValueref() instanceof CheckCast cast
                && cast.getIndex() == instanceOf.getIndex()
                && cast.getObjectref() instanceof ALoad castSource
                && instanceOf.getObjectref() instanceof ALoad tested
                && castSource.getIndex() == tested.getIndex();
    }
}
