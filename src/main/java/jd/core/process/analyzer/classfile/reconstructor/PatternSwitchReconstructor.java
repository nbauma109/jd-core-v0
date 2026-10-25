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
import java.util.function.Consumer;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.Goto;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.instruction.bytecode.instruction.LookupSwitch;
import jd.core.model.instruction.bytecode.instruction.Switch;
import jd.core.model.instruction.bytecode.instruction.TableSwitch;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.model.instruction.bytecode.instruction.LoadInstruction;
import jd.core.model.instruction.bytecode.instruction.PatternSwitchSelector;
import jd.core.model.instruction.bytecode.instruction.Pop;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.model.instruction.fast.instruction.FastDeclaration;
import jd.core.model.instruction.fast.instruction.FastList;
import jd.core.model.instruction.fast.instruction.FastSwitch;
import jd.core.model.instruction.fast.instruction.FastTest2Lists;
import jd.core.model.instruction.fast.instruction.FastTry;
import jd.core.model.instruction.fast.instruction.FastTry.FastCatch;
import jd.core.process.analyzer.classfile.visitor.SearchInstructionByTypeVisitor;
import jd.core.process.analyzer.instruction.bytecode.ComparisonInstructionAnalyzer;
import jd.core.process.analyzer.instruction.fast.visitor.CheckLocalVariableUsedVisitor;

/**
 * Reconstructs the switches on patterns compiled by javac 21 and later. The test of the
 * switch is the index of the first matching label, computed by the {@code typeSwitch}
 * bootstrap method; each case binds its pattern variable with a cast of the selector.
 */
public final class PatternSwitchReconstructor {

    private PatternSwitchReconstructor() {
    }

    /**
     * Before the structure of the method is rebuilt: the guards and the nested patterns of a case
     * jump to a block restarting the matching at the next label, which is turned into the
     * 'when' clause and the record components of the case.
     */
    public static void reconstructCases(ClassFile classFile, Method method, List<Instruction> list) {
        LocalVariables localVariables = method.getLocalVariables();
        if (localVariables == null) {
            return;
        }
        for (int s = 0; s < list.size(); s++) {
            if (list.get(s) instanceof Switch lookup && lookup.getKey() instanceof PatternSwitchSelector selector) {
                removeRestarts(list, lookup, selector);
                reconstructRecordCases(classFile, localVariables, list, lookup, selector);
                if (!selector.getRecordComponents().isEmpty()) {
                    PatternMatchingReconstructor.removeMatchExceptionHandlers(classFile, method, list);
                }
            }
        }
    }

    private static void removeRestarts(List<Instruction> list, Switch lookup, PatternSwitchSelector selector) {
        int restartVariable = selector.getRestartIndexVariable();
        for (int r = list.size() - 2; r > 0; r--) {
            if (!(list.get(r) instanceof StoreInstruction store) || store.getIndex() != restartVariable
                    || !(store.getValueref() instanceof IConst)
                    || !(list.get(r + 1) instanceof Goto jump) || jump.getJumpOffset() > lookup.getOffset()) {
                continue;
            }
            int blockStart = list.get(r - 1).getOffset();
            int blockEnd = jump.getOffset();
            selector.getBodyOffsets().merge(keyOfCase(lookup, store.getOffset()), blockEnd + 3, Math::max);
            // Conditions failing the case jump to the restart block
            for (int i = r - 1; i >= 0; i--) {
                Instruction candidate = list.get(i);
                if (candidate instanceof ConditionalBranchInstruction branch && branch.getJumpOffset() > blockStart
                        && branch.getJumpOffset() <= store.getOffset()) {
                    if (!isConstantTest(candidate)) {
                        ComparisonInstructionAnalyzer.inverseComparison(candidate);
                        addGuard(selector, keyOfCase(lookup, candidate.getOffset()), candidate);
                    }
                    list.remove(i);
                    r--;
                }
            }
            // A condition jumping over the restart block is the 'when' clause of the case
            if (r > 0 && list.get(r - 1) instanceof ConditionalBranchInstruction guard
                    && guard.getJumpOffset() > blockEnd && !isConstantTest(guard)) {
                addGuard(selector, keyOfCase(lookup, guard.getOffset()), guard);
                list.remove(r - 1);
                r--;
            }
            // A jump over the restart block
            if (list.get(r - 1) instanceof Goto over && over.getJumpOffset() > blockEnd) {
                list.remove(r - 1);
                r--;
            }
            list.subList(r, r + 2).clear();
        }
    }

    private static int keyAt(Switch sw, int index) {
        return sw instanceof LookupSwitch lookup ? lookup.getKey(index) : ((TableSwitch) sw).getLow() + index;
    }

    private static void addGuard(PatternSwitchSelector selector, int key, Instruction guard) {
        if (selector.getGuards().putIfAbsent(key, guard) != null) {
            // A guard made of several conditions would lose some of them: leave the method as byte code
            throw new IllegalStateException("Composite guard of a pattern switch");
        }
    }

    private static boolean isConstantTest(Instruction instruction) {
        return instruction instanceof IfInstruction test && test.getValue() instanceof IConst;
    }

    /** Key of the case whose code contains the given offset. */
    private static int keyOfCase(Switch lookup, int offset) {
        int bestKey = -1;
        int bestStart = -1;
        for (int i = 0; i < lookup.getOffsets().length; i++) {
            int start = lookup.getOffset() + lookup.getOffset(i);
            if (start <= offset && start > bestStart) {
                bestStart = start;
                bestKey = keyAt(lookup, i);
            }
        }
        return bestKey;
    }

    private static void reconstructRecordCases(ClassFile classFile, LocalVariables localVariables, List<Instruction> list,
            Switch lookup, PatternSwitchSelector selector) {
        for (int i = 0; i < lookup.getOffsets().length; i++) {
            int caseStart = lookup.getOffset() + lookup.getOffset(i);
            int first = 0;
            while (first < list.size() && list.get(first).getOffset() < caseStart) {
                first++;
            }
            if (first + 1 >= list.size() || !(list.get(first) instanceof StoreInstruction record)
                    || !(record.getValueref() instanceof CheckCast cast) || !(cast.getObjectref() instanceof ALoad source)
                    || source.getIndex() != selector.getIndex()) {
                continue;
            }
            List<LocalVariable> components = new ArrayList<>();
            int end = PatternMatchingReconstructor.collectComponents(classFile, localVariables, list, first + 1,
                    record.getIndex(), classFile.getConstantPool().getConstantClassName(cast.getIndex()), components);
            if (end < 0) {
                continue;
            }
            list.subList(first, end).clear();
            for (LocalVariable component : components) {
                PatternMatchingReconstructor.markBinding(classFile, localVariables, component);
            }
            selector.getRecordComponents().put(keyAt(lookup, i), components);
        }
    }

    public static void reconstruct(ClassFile classFile, Method method, List<Instruction> fastList) {
        LocalVariables localVariables = method.getLocalVariables();
        if (localVariables == null) {
            return;
        }
        FastSwitch fs;
        while ((fs = findUnprocessed(fastList)) != null) {
            process(classFile, localVariables, fastList, fs, (PatternSwitchSelector) fs.getTest());
        }
    }

    private static FastSwitch findUnprocessed(List<Instruction> fastList) {
        SearchInstructionByTypeVisitor<FastSwitch> visitor = new SearchInstructionByTypeVisitor<>(FastSwitch.class,
                fs -> fs.getPatternSelector() == null && fs.getTest() instanceof PatternSwitchSelector);
        for (Instruction instruction : fastList) {
            FastSwitch fs = visitor.visit(instruction);
            if (fs != null) {
                return fs;
            }
        }
        return null;
    }

    private static void process(ClassFile classFile, LocalVariables localVariables, List<Instruction> fastList,
            FastSwitch fs, PatternSwitchSelector selector) {
        fs.setPatternSelector(selector);
        boolean exhaustive = removeMatchExceptionDefault(classFile, fs);
        for (FastSwitch.Pair pair : fs.getPairs()) {
            List<Instruction> instructions = pair.getInstructions();
            if (pair.isDefault() || instructions == null || instructions.isEmpty()) {
                continue;
            }
            LocalVariable binding = bindingOf(localVariables, instructions.get(0), selector);
            if (binding != null) {
                instructions.remove(0);
                PatternMatchingReconstructor.markBinding(classFile, localVariables, binding);
                selector.getBindings().put(pair.getKey(), binding);
            }
        }
        // Declarations of the bound variables are part of the patterns
        removeIf(fastList, instruction -> instruction instanceof FastDeclaration declaration
                && declaration.getInstruction() == null && declaration.getLv().isPatternBinding(), localVariables, selector);
        // The variable holding the index to restart from is only needed by guards
        removeIf(fastList, instruction -> isConstantStore(instruction, selector.getRestartIndexVariable()), localVariables, selector);
        foldSelectorVariable(classFile, fastList, fs, selector);
        if (!exhaustive && isStatement(fastList, fs)) {
            addEmptyDefault(fs);
        }
    }

    private static boolean isStatement(List<Instruction> fastList, FastSwitch fs) {
        boolean[] found = new boolean[1];
        forEachList(fastList, list -> found[0] |= list.contains(fs));
        return found[0];
    }

    /**
     * A switch statement on patterns must be exhaustive. The default case is kept even if its code
     * was left after the switch.
     */
    private static void addEmptyDefault(FastSwitch fs) {
        FastSwitch.Pair[] pairs = fs.getPairs();
        for (FastSwitch.Pair pair : pairs) {
            if (pair.isDefault()) {
                if (pair.getInstructions() == null) {
                    pair.setInstructions(new ArrayList<>());
                }
                return;
            }
        }
        FastSwitch.Pair[] extended = java.util.Arrays.copyOf(pairs, pairs.length + 1);
        FastSwitch.Pair last = pairs[pairs.length - 1];
        FastSwitch.Pair empty = new FastSwitch.Pair(true, 0, last.getOffset() + 1);
        empty.setInstructions(new ArrayList<>());
        extended[pairs.length] = empty;
        fs.setPairs(extended);
    }

    /** An exhaustive switch gets a default case throwing MatchException, which source code does not have. */
    private static boolean removeMatchExceptionDefault(ClassFile classFile, FastSwitch fs) {
        ConstantPool cp = classFile.getConstantPool();
        FastSwitch.Pair[] pairs = fs.getPairs();
        for (int i = 0; i < pairs.length; i++) {
            List<Instruction> instructions = pairs[i].getInstructions();
            if (pairs[i].isDefault() && instructions != null && instructions.size() == 1
                    && instructions.get(0) instanceof AThrow thrown && thrown.getValueref() instanceof InvokeNew created
                    && "java/lang/MatchException".equals(cp.getConstantClassName(cp.getConstantMethodref(created.getIndex()).getClassIndex()))) {
                FastSwitch.Pair[] remaining = new FastSwitch.Pair[pairs.length - 1];
                System.arraycopy(pairs, 0, remaining, 0, i);
                System.arraycopy(pairs, i + 1, remaining, i, pairs.length - i - 1);
                fs.setPairs(remaining);
                return true;
            }
        }
        return false;
    }

    private static LocalVariable bindingOf(LocalVariables localVariables, Instruction first, PatternSwitchSelector selector) {
        Instruction instruction = first instanceof FastDeclaration declaration ? declaration.getInstruction() : first;
        if (instruction instanceof StoreInstruction store && store.getValueref() instanceof CheckCast cast
                && cast.getObjectref() instanceof ALoad source && source.getIndex() == selector.getIndex()) {
            return first instanceof FastDeclaration declaration ? declaration.getLv()
                    : localVariables.getLocalVariableWithIndexAndOffset(store.getIndex(), store.getOffset());
        }
        return null;
    }

    private static boolean isConstantStore(Instruction instruction, int variableIndex) {
        Instruction store = instruction instanceof FastDeclaration declaration ? declaration.getInstruction() : instruction;
        return variableIndex >= 0 && store instanceof StoreInstruction s && s.getIndex() == variableIndex
                && s.getValueref() instanceof IConst;
    }

    private static void removeIf(List<Instruction> fastList, java.util.function.Predicate<Instruction> predicate,
            LocalVariables localVariables, PatternSwitchSelector selector) {
        forEachList(fastList, list -> list.removeIf(predicate));
    }

    /**
     * Replaces the temporary variable holding the selector by the expression it was assigned from,
     * if the assignment immediately precedes the switch and nothing else uses the variable.
     */
    private static void foldSelectorVariable(ClassFile classFile, List<Instruction> fastList, FastSwitch fs,
            PatternSwitchSelector selector) {
        List<Boolean> folded = new ArrayList<>();
        forEachList(fastList, list -> {
            int switchIndex = indexOfStatementContaining(list, fs);
            if (switchIndex < 1 || !folded.isEmpty()) {
                return;
            }
            Instruction previous = list.get(switchIndex - 1);
            Instruction store = previous instanceof FastDeclaration declaration ? declaration.getInstruction() : previous;
            if (!(store instanceof StoreInstruction assignment) || assignment.getIndex() != selector.getIndex()
                    || isUsedElsewhere(fastList, selector, assignment)) {
                return;
            }
            int from = switchIndex - 1;
            if (from > 0 && isRequireNonNull(classFile, list.get(from - 1))) {
                from--;
            }
            fs.setTest(assignment.getValueref());
            list.subList(from, switchIndex).clear();
            folded.add(Boolean.TRUE);
        });
    }

    private static boolean isUsedElsewhere(List<Instruction> fastList, PatternSwitchSelector selector, StoreInstruction assignment) {
        java.util.function.Predicate<IndexInstruction> uses = instruction ->
                instruction.getIndex() == selector.getIndex() && instruction instanceof LoadInstruction
                        && !(instruction instanceof StoreInstruction) && instruction != selector;
        for (Instruction instruction : fastList) {
            if (CheckLocalVariableUsedVisitor.visit(uses, instruction)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRequireNonNull(ClassFile classFile, Instruction instruction) {
        Instruction call = instruction instanceof Pop pop ? pop.getObjectref() : instruction;
        if (call instanceof Invokestatic invoke) {
            ConstantPool cp = classFile.getConstantPool();
            var methodref = cp.getConstantMethodref(invoke.getIndex());
            return "java/util/Objects".equals(cp.getConstantClassName(methodref.getClassIndex()))
                    && "requireNonNull".equals(cp.getConstantUtf8(cp.getConstantNameAndType(methodref.getNameAndTypeIndex()).getNameIndex()));
        }
        return false;
    }

    private static int indexOfStatementContaining(List<Instruction> list, FastSwitch fs) {
        SearchInstructionByTypeVisitor<FastSwitch> visitor = new SearchInstructionByTypeVisitor<>(FastSwitch.class, candidate -> candidate == fs);
        for (int i = 0; i < list.size(); i++) {
            if (visitor.visit(list.get(i)) != null) {
                return i;
            }
        }
        return -1;
    }

    private static void forEachList(List<Instruction> list, Consumer<List<Instruction>> action) {
        action.accept(list);
        for (Instruction instruction : new ArrayList<>(list)) {
            if (instruction instanceof FastList fastList && fastList.getInstructions() != null) {
                forEachList(fastList.getInstructions(), action);
            }
            if (instruction instanceof FastTest2Lists test && test.getInstructions2() != null) {
                forEachList(test.getInstructions2(), action);
            }
            if (instruction instanceof FastTry fastTry) {
                for (FastCatch fastCatch : fastTry.getCatches()) {
                    forEachList(fastCatch.instructions(), action);
                }
                if (fastTry.getFinallyInstructions() != null) {
                    forEachList(fastTry.getFinallyInstructions(), action);
                }
            }
            if (instruction instanceof FastSwitch fastSwitch && fastSwitch.getPairs() != null) {
                for (FastSwitch.Pair pair : fastSwitch.getPairs()) {
                    if (pair.getInstructions() != null) {
                        forEachList(pair.getInstructions(), action);
                    }
                }
            }
        }
    }
}
