/*******************************************************************************
 * Copyright (C) 2026 GPLv3
 *
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
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.AConstNull;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.AStore;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ExceptionLoad;
import jd.core.model.instruction.bytecode.instruction.Goto;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;

/**
 * Reconstructs the try-with-resources statements compiled by ECJ, which keeps the exception to throw in a variable
 * shared by all the resources of the statement:
 *
 * <pre>
 * Throwable primary = null; Throwable suppressed = null;
 * R1 r1 = init1; R2 r2 = init2;
 * body; if (r2 != null) r2.close();                          // on every exit of the body
 * catch (any t) { primary = t; if (r2 != null) r2.close(); throw primary; }
 * if (r1 != null) r1.close();                                // on the normal exit
 * catch (any t) { merge t into primary; if (r1 != null) r1.close(); throw primary; }
 * catch (any t) { merge t into primary; throw primary; }
 * </pre>
 *
 * Like {@link TryWithResourcesReconstructor}, it removes the cleanup from the instructions and from the exception table,
 * and leaves {@link TryWithResourcesReconstructor#wrap} to build the statement.
 */
public final class EcjTryWithResourcesReconstructor {

    private EcjTryWithResourcesReconstructor() {
    }

    /** The instructions of the cleanup of one statement. */
    private static final class Cleanup {
        /** The variables holding the resources, the innermost first. */
        private final List<Integer> resources = new ArrayList<>();
        /** The offsets of the exception handlers. */
        private final List<Integer> handlers = new ArrayList<>();
        /** The index of the first handler, which ends the body. */
        private int start;
        /** The index of the last instruction of the last handler. */
        private int end;
        /** The jump leaving the statement when the body completes normally, if any. */
        private Goto exit;
    }

    public static void reconstruct(ClassFile classFile, Method method, List<Instruction> list) {
        CodeException[] exceptions = method.getCodeExceptions();
        if (exceptions == null || exceptions.length < 2) {
            return;
        }
        ConstantPool cp = classFile.getConstantPool();
        // The last statements first: removing their instructions does not move the previous ones
        for (int i = list.size() - 2; i >= 0; i--) {
            if (i + 1 < list.size() && isNullInitialization(list.get(i)) && isNullInitialization(list.get(i + 1))) {
                reconstruct(cp, method, list, i);
            }
        }
    }

    private static boolean isNullInitialization(Instruction instruction) {
        return instruction instanceof AStore store && store.getValueref() instanceof AConstNull;
    }

    private static void reconstruct(ConstantPool cp, Method method, List<Instruction> list, int prologue) {
        int primary = ((AStore) list.get(prologue)).getIndex();
        int suppressed = ((AStore) list.get(prologue + 1)).getIndex();
        Cleanup cleanup = findCleanup(cp, list, prologue + 2, primary, suppressed);
        if (cleanup == null) {
            return;
        }
        List<int[]> declarations = new ArrayList<>();
        for (int resource : cleanup.resources) {
            int declaration = indexOfStore(list, prologue + 2, cleanup.start, resource);
            if (declaration < 0) {
                return;
            }
            declarations.add(new int[] {resource, list.get(declaration).getOffset(), list.get(cleanup.start).getOffset()});
        }
        List<CodeException> remaining = new ArrayList<>(List.of(method.getCodeExceptions()));
        if (!remaining.removeIf(e -> e.getCatchType() == 0 && cleanup.handlers.contains(e.getHandlerPC()))) {
            return;
        }
        Instruction last = list.get(cleanup.end);
        list.subList(cleanup.start, cleanup.end + 1).clear();
        // A jump over the code following the last "athrow", such as the catch blocks of the statement, now ends the body
        if (cleanup.exit != null && cleanup.exit.getJumpOffset() > last.getOffset() + 1) {
            list.add(cleanup.start, new Goto(Const.GOTO, last.getOffset(), cleanup.exit.getLineNumber(),
                    cleanup.exit.getJumpOffset() - last.getOffset()));
        }
        removeExitCleanups(cp, method.getCodeExceptions(), list, prologue + 2, cleanup);
        list.subList(prologue, prologue + 2).clear();
        method.setCodeExceptions(remaining.toArray(new CodeException[0]));
        method.getTryResources().addAll(declarations);
    }

    /**
     * Reads the handlers of the statement: the one of the innermost resource, then one for each of the other
     * resources, preceded by its call of close() on the normal exit, and the last one which closes nothing.
     */
    private static Cleanup findCleanup(ConstantPool cp, List<Instruction> list, int from, int primary, int suppressed) {
        for (int start = from; start < list.size(); start++) {
            Cleanup cleanup = readBodyHandler(cp, list, start, primary);
            if (cleanup != null) {
                return readOuterHandlers(cp, list, cleanup.end + 1, primary, suppressed, cleanup);
            }
        }
        return null;
    }

    /** Reads the handler of the innermost resource, which may start at the given index. */
    private static Cleanup readBodyHandler(ConstantPool cp, List<Instruction> list, int start, int primary) {
        if (!isCatch(list.get(start), primary)) {
            return null;
        }
        Cleanup cleanup = new Cleanup();
        cleanup.end = readClose(cp, list, start + 1, cleanup.resources);
        if (cleanup.resources.isEmpty() || !isThrow(list, cleanup.end, primary)) {
            return null;
        }
        cleanup.start = start;
        cleanup.handlers.add(list.get(start).getOffset());
        if (list.get(start - 1) instanceof Goto jump && jump.getJumpOffset() > list.get(start).getOffset()) {
            cleanup.exit = jump;
            cleanup.start--;
        }
        return cleanup;
    }

    private static Cleanup readOuterHandlers(ConstantPool cp, List<Instruction> list, int from, int primary,
            int suppressed, Cleanup cleanup) {
        int index = skipNormalExits(cp, list, from, suppressed, cleanup);
        while (index >= 0 && isMerge(cp, list, index, primary, suppressed)) {
            cleanup.handlers.add(list.get(index).getOffset());
            List<Integer> closed = new ArrayList<>();
            index = readClose(cp, list, index + 6, closed);
            if (!isThrow(list, index, primary)) {
                return null;
            }
            if (closed.isEmpty()) {
                cleanup.end = index;
                return cleanup;
            }
            cleanup.resources.add(closed.get(0));
            index = skipNormalExits(cp, list, index + 1, suppressed, cleanup);
        }
        return null;
    }

    /**
     * Skips the calls of close() of the outer resources, when the body completes normally.
     *
     * @return the index of the following handler, or -1 if other instructions come first
     */
    private static int skipNormalExits(ConstantPool cp, List<Instruction> list, int from, int suppressed,
            Cleanup cleanup) {
        int index = from;
        while (index < list.size() && !isCatch(list.get(index), suppressed)) {
            if (list.get(index) instanceof Goto jump) {
                cleanup.exit = jump;
                index++;
            } else {
                int next = readClose(cp, list, index, new ArrayList<>());
                if (next == index) {
                    return -1;
                }
                index = next;
            }
        }
        return index;
    }

    /** Reads "if (resource != null) resource.close();", whose test is optional, and returns the following index. */
    private static int readClose(ConstantPool cp, List<Instruction> list, int index, List<Integer> resources) {
        int call = index < list.size() && list.get(index) instanceof IfInstruction ? index + 1 : index;
        if (call < list.size()) {
            int resource = TryWithResourcesReconstructor.closedResource(cp, list.get(call));
            if (resource >= 0 && (call == index || isTestOf(list.get(index), resource))) {
                resources.add(resource);
                return call + 1;
            }
        }
        return index;
    }

    private static boolean isTestOf(Instruction instruction, int variable) {
        return instruction instanceof IfInstruction test && isLoad(test.getValue(), variable);
    }

    private static boolean isLoad(Instruction instruction, int variable) {
        return instruction instanceof ALoad load && load.getIndex() == variable;
    }

    private static boolean isCatch(Instruction instruction, int variable) {
        return instruction instanceof AStore store && store.getIndex() == variable
                && store.getValueref() instanceof ExceptionLoad;
    }

    private static boolean isThrow(List<Instruction> list, int index, int variable) {
        return index < list.size() && list.get(index) instanceof AThrow athrow && isLoad(athrow.getValueref(), variable);
    }

    /** "if (primary == null) primary = suppressed; else if (primary != suppressed) primary.addSuppressed(suppressed);" */
    private static boolean isMerge(ConstantPool cp, List<Instruction> list, int index, int primary, int suppressed) {
        return index + 5 < list.size()
                && isTestOf(list.get(index + 1), primary)
                && list.get(index + 2) instanceof AStore store && store.getIndex() == primary
                && isLoad(store.getValueref(), suppressed)
                && list.get(index + 3) instanceof Goto
                && list.get(index + 4) instanceof IfCmp compare && isLoad(compare.getValue1(), primary)
                && isLoad(compare.getValue2(), suppressed)
                && TryWithResourcesReconstructor.isAddSuppressed(cp, list.get(index + 5), primary, suppressed);
    }

    private static int indexOfStore(List<Instruction> list, int from, int to, int variable) {
        for (int i = from; i < to; i++) {
            if (list.get(i) instanceof AStore store && store.getIndex() == variable) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Removes the calls of close() on the exits of the body. The compiler leaves them out of the ranges protected by
     * the handlers, unlike the calls written in the body.
     */
    private static void removeExitCleanups(ConstantPool cp, CodeException[] exceptions, List<Instruction> list,
            int from, Cleanup cleanup) {
        int bodyHandler = cleanup.handlers.get(0);
        int i = Math.min(list.size(), cleanup.start + 1) - 1;
        while (i >= from) {
            Instruction instruction = list.get(i);
            int offset = instruction.getOffset();
            int resource = TryWithResourcesReconstructor.closedResource(cp, instruction);
            if (offset < bodyHandler && cleanup.resources.contains(resource)
                    && !isProtected(exceptions, bodyHandler, offset)) {
                list.remove(i);
                if (i > from && isTestOf(list.get(i - 1), resource)) {
                    list.remove(--i);
                }
            }
            i--;
        }
    }

    private static boolean isProtected(CodeException[] exceptions, int handler, int offset) {
        for (CodeException exception : exceptions) {
            if (exception.getHandlerPC() == handler && exception.getStartPC() <= offset && offset < exception.getEndPC()) {
                return true;
            }
        }
        return false;
    }
}
