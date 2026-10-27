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
                recordPattern |= reconstructRecordPattern(classFile, method, localVariables, list, i, instanceOf);
            }
        }
        if (recordPattern) {
            removeMatchExceptionHandlers(classFile, method, list);
        }
        for (int i = list.size() - 2; i >= 0; i--) {
            if (list.get(i) instanceof IfInstruction test && test.getValue() instanceof InstanceOf instanceOf
                    && test.getCmp() == ByteCodeConstants.CMP_EQ && instanceOf.getPatternVariable() == null
                    && list.get(i + 1) instanceof AStore store && isBinding(instanceOf, store)
                    && !readsRecordComponents(classFile, list, i + 2, store.getIndex())) {
                LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(store.getIndex(), store.getOffset());
                if (lv != null) {
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
    private static boolean reconstructRecordPattern(ClassFile classFile, Method method, LocalVariables localVariables,
            List<Instruction> list, int ifIndex, InstanceOf instanceOf) {
        if (!(instanceOf.getObjectref() instanceof ALoad tested) || ifIndex + 2 >= list.size()
                || !(list.get(ifIndex + 1) instanceof AStore recordStore) || !(unwrapCast(recordStore.getValueref()) instanceof ALoad source)
                || source.getIndex() != tested.getIndex()) {
            return false;
        }
        List<LocalVariable> bindings = new ArrayList<>();
        int end = collectComponents(classFile, localVariables, list, ifIndex + 2, recordStore.getIndex(),
                classFile.getConstantPool().getConstantClassName(instanceOf.getIndex()), bindings);
        if (end < 0) {
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
    static int collectComponents(ClassFile classFile, LocalVariables localVariables, List<Instruction> list,
            int start, int recordVariable, String recordName, List<LocalVariable> bindings) {
        ConstantPool cp = classFile.getConstantPool();
        if (!isRecord(classFile, recordName)) {
            return -1;
        }
        int end = start;
        while (end < list.size() && list.get(end) instanceof StoreInstruction component
                && component.getValueref() instanceof InvokeNoStaticInstruction accessor
                && accessor.getArgs().isEmpty()
                && accessor.getObjectref() instanceof ALoad owner && owner.getIndex() == recordVariable
                && recordName.equals(cp.getConstantClassName(cp.getConstantMethodref(accessor.getIndex()).getClassIndex()))) {
            int next = end + 1;
            StoreInstruction binding = null;
            while (next < list.size()) {
                Instruction instruction = list.get(next);
                if (instruction instanceof StoreInstruction copy && copy.getValueref() instanceof LoadInstruction load
                        && load.getIndex() == component.getIndex()) {
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
            if (lv == null) {
                return -1;
            }
            bindings.add(lv);
            end = next;
        }
        return bindings.isEmpty() ? -1 : end;
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

    static void removeMatchExceptionHandlers(ClassFile classFile, Method method, List<Instruction> list) {
        CodeException[] exceptions = method.getCodeExceptions();
        if (exceptions == null) {
            return;
        }
        ConstantPool cp = classFile.getConstantPool();
        List<CodeException> remaining = new ArrayList<>(List.of(exceptions));
        for (CodeException exception : exceptions) {
            int handler = -1;
            for (int i = 0; i < list.size() - 1; i++) {
                if (list.get(i).getOffset() == exception.getHandlerPC()) {
                    handler = i;
                    break;
                }
            }
            if (handler > 0 && list.get(handler) instanceof AStore store && store.getValueref() instanceof ExceptionLoad
                    && list.get(handler + 1) instanceof AThrow thrown && thrown.getValueref() instanceof InvokeNew created
                    && "java/lang/MatchException".equals(cp.getConstantClassName(cp.getConstantMethodref(created.getIndex()).getClassIndex()))) {
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

    /** A range of the same variable starts after a jump, not after the store of a new variable. */
    private static boolean isDeclaredByStore(byte[] code, LocalVariable lv) {
        int start = lv.getStartPc();
        if (code == null || start < 1 || start > code.length) {
            return false;
        }
        int previous = code[start - 1] & 255;
        boolean shortStore = previous >= Const.ASTORE_0 && previous <= Const.ASTORE_3 && previous - Const.ASTORE_0 == lv.getIndex();
        boolean longStore = start >= 2 && (code[start - 2] & 255) == Const.ASTORE && previous == lv.getIndex();
        // The range of a variable may have been extended to its first store
        int current = start < code.length ? code[start] & 255 : 0;
        boolean storeAtStart = current >= Const.ASTORE_0 && current <= Const.ASTORE_3 && current - Const.ASTORE_0 == lv.getIndex()
                || current == Const.ASTORE && start + 1 < code.length && (code[start + 1] & 255) == lv.getIndex();
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
