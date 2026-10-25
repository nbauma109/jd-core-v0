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
import java.util.Comparator;
import java.util.List;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantNameAndType;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.AStore;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ExceptionLoad;
import jd.core.model.instruction.bytecode.instruction.Goto;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.fast.FastConstants;
import jd.core.model.instruction.fast.instruction.FastDeclaration;
import jd.core.model.instruction.fast.instruction.FastList;
import jd.core.model.instruction.fast.instruction.FastTry;
import jd.core.model.instruction.fast.instruction.FastTry.FastCatch;

/**
 * Reconstructs the try-with-resources statements compiled by javac 11 and later:
 *
 * <pre>
 * R r = init;
 * try { body; r.close(); }
 * catch (Throwable t) {
 *     try { r.close(); } catch (Throwable t2) { t.addSuppressed(t2); }
 *     throw t;
 * }
 * </pre>
 *
 * The cleanup plumbing is removed from the instructions and from the exception
 * table before the structure of the method is rebuilt, then
 * {@link #wrap(Method, List)} puts the body under a resource-only {@link FastTry}.
 */
// Linear matching of a fixed bytecode shape
@SuppressWarnings("java:S3776")
public final class TryWithResourcesReconstructor {

    private static final String THROWABLE = "java/lang/Throwable";

    private TryWithResourcesReconstructor() {
    }

    public static void reconstruct(ClassFile classFile, Method method, List<Instruction> list) {
        CodeException[] exceptions = method.getCodeExceptions();
        if (exceptions == null || exceptions.length < 2) {
            return;
        }
        List<CodeException> remaining = new ArrayList<>(List.of(exceptions));
        boolean changed = true;
        while (changed) {
            changed = false;
            for (CodeException guarded : remaining) {
                if (reconstruct(classFile, method, list, remaining, guarded)) {
                    changed = true;
                    break;
                }
            }
        }
        method.setCodeExceptions(remaining.toArray(new CodeException[0]));
    }

    private static boolean reconstruct(ClassFile classFile, Method method, List<Instruction> list,
            List<CodeException> exceptions, CodeException guarded) {
        ConstantPool cp = classFile.getConstantPool();
        if (!isThrowable(cp, guarded)) {
            return false;
        }
        int handlerIndex = indexOfOffset(list, guarded.getHandlerPC());
        if (handlerIndex < 1 || !(list.get(handlerIndex) instanceof AStore store)
                || !(store.getValueref() instanceof ExceptionLoad)) {
            return false;
        }
        int closeIndex = handlerIndex + 1;
        if (closeIndex < list.size() && list.get(closeIndex) instanceof IfInstruction) {
            closeIndex++;
        }
        if (closeIndex >= list.size()) {
            return false;
        }
        int resourceIndex = closedResource(cp, list.get(closeIndex));
        if (resourceIndex < 0) {
            return false;
        }
        int closeOffset = list.get(closeIndex).getOffset();
        CodeException cleanup = null;
        for (CodeException candidate : exceptions) {
            if (candidate != guarded && isThrowable(cp, candidate)
                    && candidate.getStartPC() >= guarded.getHandlerPC()
                    && candidate.getStartPC() <= closeOffset && closeOffset < candidate.getEndPC()) {
                cleanup = candidate;
                break;
            }
        }
        if (cleanup == null) {
            return false;
        }
        int suppressedIndex = indexOfOffset(list, cleanup.getHandlerPC());
        int throwIndex = suppressedIndex + 2;
        if (suppressedIndex < 0 || throwIndex >= list.size()
                || !(list.get(suppressedIndex) instanceof AStore suppressedStore)
                || !isAddSuppressed(cp, list.get(suppressedIndex + 1), store.getIndex(), suppressedStore.getIndex())
                || !(list.get(throwIndex) instanceof AThrow athrow)
                || !(athrow.getValueref() instanceof ALoad thrown) || thrown.getIndex() != store.getIndex()) {
            return false;
        }
        int firstBodyIndex = firstIndexFromOffset(list, guarded.getStartPC());
        if (firstBodyIndex < 1 || !(list.get(firstBodyIndex - 1) instanceof AStore declaration)
                || declaration.getIndex() != resourceIndex) {
            return false;
        }

        int declarationOffset = declaration.getOffset();
        int throwOffset = athrow.getOffset();
        // Remove the cleanup handler and the jump over it
        if (list.get(handlerIndex - 1) instanceof Goto jump && jump.getJumpOffset() > throwOffset) {
            handlerIndex--;
        }
        list.subList(handlerIndex, throwIndex + 1).clear();
        // Remove the inlined calls of close() on every exit of the body
        for (int i = list.size() - 1; i > firstBodyIndex - 1; i--) {
            Instruction instruction = list.get(i);
            if (instruction.getOffset() < guarded.getHandlerPC() && closedResource(cp, instruction) == resourceIndex
                    && isExitCleanup(list, i, guarded.getHandlerPC())) {
                list.remove(i);
                if (i > 0 && list.get(i - 1) instanceof IfInstruction nullCheck
                        && nullCheck.getValue() instanceof ALoad checked && checked.getIndex() == resourceIndex) {
                    list.remove(i - 1);
                }
            }
        }
        exceptions.remove(guarded);
        exceptions.remove(cleanup);
        method.getTryResources().add(new int[] {resourceIndex, declarationOffset, guarded.getHandlerPC()});
        return true;
    }

    /** A close() followed by more code of the body was written by the user, not generated by the compiler. */
    private static boolean isExitCleanup(List<Instruction> list, int index, int handlerOffset) {
        if (index + 1 >= list.size() || list.get(index + 1).getOffset() >= handlerOffset) {
            return true;
        }
        int next = list.get(index + 1).getOpcode();
        return next == Const.RETURN || next == ByteCodeConstants.XRETURN || next == Const.GOTO || next == Const.ATHROW;
    }

    private static boolean isThrowable(ConstantPool cp, CodeException exception) {
        if (exception.getCatchType() == 0) {
            return false;
        }
        ConstantClass constantClass = cp.getConstantClass(exception.getCatchType());
        return THROWABLE.equals(cp.getConstantUtf8(constantClass.getNameIndex()));
    }

    private static int indexOfOffset(List<Instruction> list, int offset) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getOffset() == offset) {
                return i;
            }
        }
        return -1;
    }

    private static int firstIndexFromOffset(List<Instruction> list, int offset) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getOffset() >= offset) {
                return i;
            }
        }
        return -1;
    }

    /** Index of the local variable closed by a call of {@code close()}, or -1. */
    private static int closedResource(ConstantPool cp, Instruction instruction) {
        if (instruction instanceof InvokeNoStaticInstruction invoke && invoke.getArgs().isEmpty()
                && invoke.getObjectref() instanceof ALoad receiver
                && "close".equals(methodName(cp, invoke)) && "()V".equals(methodDescriptor(cp, invoke))) {
            return receiver.getIndex();
        }
        return -1;
    }

    private static boolean isAddSuppressed(ConstantPool cp, Instruction instruction, int primary, int suppressed) {
        return instruction instanceof InvokeNoStaticInstruction invoke && invoke.getArgs().size() == 1
                && invoke.getObjectref() instanceof ALoad receiver && receiver.getIndex() == primary
                && invoke.getArgs().get(0) instanceof ALoad argument && argument.getIndex() == suppressed
                && "addSuppressed".equals(methodName(cp, invoke));
    }

    private static String methodName(ConstantPool cp, InvokeNoStaticInstruction invoke) {
        ConstantCP methodref = cp.getConstantMethodref(invoke.getIndex());
        ConstantNameAndType nameAndType = cp.getConstantNameAndType(methodref.getNameAndTypeIndex());
        return cp.getConstantUtf8(nameAndType.getNameIndex());
    }

    private static String methodDescriptor(ConstantPool cp, InvokeNoStaticInstruction invoke) {
        ConstantCP methodref = cp.getConstantMethodref(invoke.getIndex());
        ConstantNameAndType nameAndType = cp.getConstantNameAndType(methodref.getNameAndTypeIndex());
        return cp.getConstantUtf8(nameAndType.getSignatureIndex());
    }

    /**
     * Puts the body of every reconstructed try-with-resources statement under
     * a resource-only {@link FastTry}. Returns false if a declaration is missing.
     */
    public static boolean wrap(Method method, List<Instruction> fastList) {
        List<int[]> resources = new ArrayList<>(method.getTryResources());
        resources.sort(Comparator.comparingInt((int[] resource) -> resource[1]).reversed());
        boolean complete = true;
        for (int[] resource : resources) {
            complete &= wrap(method.getLocalVariables(), fastList, resource);
            removeHoistedDeclarations(fastList, resource[0]);
        }
        return complete;
    }

    /** The resource is declared by the statement: a declaration hoisted before it would declare it twice. */
    private static void removeHoistedDeclarations(List<Instruction> list, int variableIndex) {
        list.removeIf(instruction -> instruction instanceof FastDeclaration declaration
                && declaration.getInstruction() == null && declaration.getLv().getIndex() == variableIndex
                && declaration.getLv().hasDeclarationFlag());
        for (Instruction instruction : new ArrayList<>(list)) {
            for (List<Instruction> nested : nestedLists(instruction)) {
                removeHoistedDeclarations(nested, variableIndex);
            }
        }
    }

    private static boolean wrap(LocalVariables localVariables, List<Instruction> list, int[] resource) {
        for (int i = 0; i < list.size(); i++) {
            Instruction instruction = list.get(i);
            FastDeclaration declaration = asDeclaration(localVariables, instruction, resource);
            if (declaration != null) {
                int end = i + 1;
                while (end < list.size() && minOffset(list.get(end)) < resource[2]) {
                    end++;
                }
                List<Instruction> body = new ArrayList<>(list.subList(i + 1, end));
                list.subList(i, end).clear();
                int lastOffset = body.isEmpty() ? declaration.getOffset() : body.get(body.size() - 1).getOffset();
                FastTry fastTry = new FastTry(FastConstants.TRY, lastOffset, declaration.getLineNumber(), 0,
                        body, new ArrayList<>(), null);
                fastTry.getResources().add(declaration);
                declaration.getLv().setDeclarationFlag(true);
                fastTry.processTryResources();
                list.add(i, fastTry);
                return true;
            }
            for (List<Instruction> nested : nestedLists(instruction)) {
                if (wrap(localVariables, nested, resource)) {
                    // A try statement whose only content is the new statement holds its resources
                    if (instruction instanceof FastTry parent) {
                        parent.processTryResources();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /** The declaration of the resource, even if the analysis left it as a plain store. */
    private static FastDeclaration asDeclaration(LocalVariables localVariables, Instruction instruction, int[] resource) {
        if (instruction.getOffset() != resource[1]) {
            return null;
        }
        if (instruction instanceof FastDeclaration declaration) {
            return declaration.getInstruction() == null ? null : declaration;
        }
        if (instruction instanceof AStore store && store.getIndex() == resource[0] && localVariables != null) {
            LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(store.getIndex(), store.getOffset());
            if (lv != null) {
                return new FastDeclaration(store.getOffset(), store.getLineNumber(), lv, store);
            }
        }
        return null;
    }

    private static List<List<Instruction>> nestedLists(Instruction instruction) {
        List<List<Instruction>> lists = new ArrayList<>();
        if (instruction instanceof FastList fastList && fastList.getInstructions() != null) {
            lists.add(fastList.getInstructions());
        }
        if (instruction instanceof jd.core.model.instruction.fast.instruction.FastTest2Lists test2Lists
                && test2Lists.getInstructions2() != null) {
            lists.add(test2Lists.getInstructions2());
        }
        if (instruction instanceof jd.core.model.instruction.fast.instruction.FastSwitch fastSwitch && fastSwitch.getPairs() != null) {
            for (jd.core.model.instruction.fast.instruction.FastSwitch.Pair pair : fastSwitch.getPairs()) {
                if (pair.getInstructions() != null) {
                    lists.add(pair.getInstructions());
                }
            }
        }
        if (instruction instanceof FastTry fastTry) {
            for (FastCatch fastCatch : fastTry.getCatches()) {
                lists.add(fastCatch.instructions());
            }
            if (fastTry.getFinallyInstructions() != null) {
                lists.add(fastTry.getFinallyInstructions());
            }
        }
        return lists;
    }

    private static int minOffset(Instruction instruction) {
        int min = instruction.getOffset();
        if (instruction instanceof FastList || instruction instanceof FastTry) {
            for (List<Instruction> nested : nestedLists(instruction)) {
                for (Instruction child : nested) {
                    min = Math.min(min, minOffset(child));
                }
            }
        }
        return min;
    }
}
