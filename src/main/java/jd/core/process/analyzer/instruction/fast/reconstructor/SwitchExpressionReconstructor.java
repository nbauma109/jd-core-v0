/**
 * Copyright (C) 2007-2025 Emmanuel Dupuy GPLv3 and other contributors
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
 */
package jd.core.process.analyzer.instruction.fast.reconstructor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.LineNumber;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.BranchInstruction;
import jd.core.model.instruction.bytecode.instruction.Goto;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.PatternSwitchSelector;
import jd.core.model.instruction.bytecode.instruction.Pop;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.model.instruction.bytecode.instruction.TernaryOpStore;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.model.instruction.fast.FastConstants;
import jd.core.model.instruction.fast.instruction.FastDeclaration;
import jd.core.model.instruction.fast.instruction.FastFor;
import jd.core.model.instruction.fast.instruction.FastForEach;
import jd.core.model.instruction.fast.instruction.FastInstruction;
import jd.core.model.instruction.fast.instruction.FastLabel;
import jd.core.model.instruction.fast.instruction.FastList;
import jd.core.model.instruction.fast.instruction.FastSwitch;
import jd.core.model.instruction.fast.instruction.FastSynchronized;
import jd.core.model.instruction.fast.instruction.FastTest2Lists;
import jd.core.model.instruction.fast.instruction.FastTestList;
import jd.core.model.instruction.fast.instruction.FastTry;
import jd.core.model.instruction.fast.instruction.FastTry.FastCatch;
import jd.core.process.analyzer.instruction.bytecode.ComparisonInstructionAnalyzer;
import jd.core.process.analyzer.instruction.bytecode.factory.GotoFactory;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactoryConstants;
import jd.core.process.analyzer.instruction.bytecode.util.ByteCodeUtil;
import jd.core.process.analyzer.instruction.fast.visitor.ReplaceInstructionVisitor;

public final class SwitchExpressionReconstructor {

    private SwitchExpressionReconstructor() {
    }

    public static void reconstruct(ClassFile classFile, Method method, List<Instruction> list) {
        if (list == null || list.isEmpty() || classFile == null || method == null) {
            return;
        }

        // From the last one, so that a switch expression precedes the one following it in the same statement
        for (int index = list.size() - 1; index >= 0; index--) {
            Instruction instruction = list.get(index);

            if (instruction instanceof FastSwitch fs) {
                int targetIndex = findNextTargetIndex(list, index + 1);
                if (targetIndex != -1) {
                    Instruction targetInstruction = list.get(targetIndex);
                    if (tryReplaceWithSwitchExpression(classFile, method, fs, targetInstruction)) {
                        list.remove(index);
                        continue;
                    }
                }
            }

            for (List<Instruction> block : getBlocks(instruction)) {
                reconstruct(classFile, method, block);
            }
        }
    }

    private static boolean tryReplaceWithSwitchExpression(ClassFile classFile, Method method, FastSwitch fs, Instruction targetInstruction) {
        Instruction target = unwrapTarget(targetInstruction);
        // The results of the switch expressions which are the values of cases
        Map<FastSwitch, List<Instruction>> nested = new IdentityHashMap<>();
        List<Instruction> results = resolveCaseResults(classFile, method, fs, fs.getJumpOffset(), nested);
        // The instructions following the switch consume the value of the last case
        Instruction consumed = lastValue(results, nested);
        if (consumed == null) {
            return false;
        }

        if (target instanceof StoreInstruction si && isComputedBySwitch(fs, si.getValueref())) {
            forceConstantSignature(si.getReturnedSignature(classFile, method.getLocalVariables()), results, nested);
            si.setValueref(fs);
        } else if (target instanceof ReturnInstruction ri && isComputedBySwitch(fs, ri.getValueref())) {
            forceConstantSignature(ri.getReturnedSignature(classFile, method.getLocalVariables()), results, nested);
            ri.setValueref(fs);
        } else {
            Instruction replaced = isDiscardedBySwitch(fs, target) ? null : replace(target, consumed.getOffset(), fs);
            if (replaced == null) {
                return false;
            }
            // The type expected by the instruction consuming the value has been given to the value it consumed
            forceConstantSignature(replaced.getReturnedSignature(classFile, method.getLocalVariables()), results, nested);
        }

        addCaseResults(fs, results, nested);
        alignTargetLineNumber(fs, targetInstruction);
        if (target != targetInstruction) {
            alignTargetLineNumber(fs, target);
        }
        return true;
    }

    private static Instruction unwrapTarget(Instruction instruction) {
        if (instruction instanceof FastLabel fl && fl.getInstruction() != null) {
            return unwrapTarget(fl.getInstruction());
        }
        if (instruction instanceof FastDeclaration fd) {
            return fd.getInstruction();
        }
        return instruction;
    }

    /** @return true if the value of the switch is discarded at once, which a switch expression cannot be */
    private static boolean isDiscardedBySwitch(FastSwitch fs, Instruction target) {
        return target instanceof Pop pop && isComputedBySwitch(fs, pop.getObjectref());
    }

    /** @return the instruction replaced by the switch, null if there is none */
    private static Instruction replace(Instruction target, int offset, FastSwitch fs) {
        ReplaceInstructionVisitor visitor = new ReplaceInstructionVisitor(offset, fs);
        visitor.visit(target);
        return visitor.getOldInstruction();
    }

    /** @return false if the value is computed after the switch, from the one it gives */
    private static boolean isComputedBySwitch(FastSwitch fs, Instruction value) {
        return value.getOffset() < fs.getJumpOffset();
    }

    /** @return the value of each case, null for the cases throwing; nothing if the switch is not an expression */
    private static List<Instruction> resolveCaseResults(ClassFile classFile, Method method, FastSwitch fs, int breakOffset,
            Map<FastSwitch, List<Instruction>> nested) {
        FastSwitch.Pair[] pairs = fs.getPairs();
        List<Instruction> results = new ArrayList<>(pairs.length);

        for (FastSwitch.Pair pair : pairs) {
            Instruction last = findLastNonBreakInstruction(pair.getInstructions());
            if (last != null && last.getOpcode() == Const.ATHROW) {
                results.add(null);
                continue;
            }

            int start = bodyOffset(fs, pair);
            Instruction result = resolveCaseResultFromBytecode(classFile, method, start, breakOffset, branchEnd(fs, start));
            if (result == null) {
                result = resolveNestedSwitch(classFile, method, last, breakOffset, nested);
            }
            if (result == null) {
                return List.of();
            }
            results.add(result);
        }

        return results;
    }

    /** @return the instruction if it is a switch expression giving the value of a case, null otherwise */
    private static Instruction resolveNestedSwitch(ClassFile classFile, Method method, Instruction instruction, int breakOffset,
            Map<FastSwitch, List<Instruction>> nested) {
        if (!(instruction instanceof FastSwitch fs)) {
            return null;
        }
        // The cases of the nested expression jump to the end of the enclosing one
        List<Instruction> results = resolveCaseResults(classFile, method, fs, breakOffset, nested);
        if (lastValue(results, nested) == null) {
            return null;
        }
        nested.put(fs, results);
        return fs;
    }

    /** @return the last value computed by these results, null if there is none */
    private static Instruction lastValue(List<Instruction> results, Map<FastSwitch, List<Instruction>> nested) {
        return results.stream().filter(Objects::nonNull)
                .map(result -> nested.containsKey(result) ? lastValue(nested.get(result), nested) : result)
                .max(Comparator.comparingInt(Instruction::getOffset)).orElse(null);
    }

    /** Writes the constants as booleans or characters, rather than as numbers, where such a value is expected. */
    private static void forceConstantSignature(String targetSignature, List<Instruction> results,
            Map<FastSwitch, List<Instruction>> nested) {
        if ("Z".equals(targetSignature) || "C".equals(targetSignature)) {
            results.forEach(result -> setConstantSignature(targetSignature, result));
            nested.values().forEach(values -> values.forEach(value -> setConstantSignature(targetSignature, value)));
        }
    }

    private static void setConstantSignature(String signature, Instruction value) {
        if (value instanceof IConst iConst) {
            iConst.setSignature(signature);
        } else if (value instanceof TernaryOperator ternary) {
            setConstantSignature(signature, ternary.getValue1());
            setConstantSignature(signature, ternary.getValue2());
        }
    }

    private static void addCaseResults(FastSwitch fs, List<Instruction> results, Map<FastSwitch, List<Instruction>> nested) {
        FastSwitch.Pair[] pairs = fs.getPairs();
        for (int i = 0; i < pairs.length; i++) {
            FastSwitch.Pair pair = pairs[i];
            List<Instruction> instructions = pair.getInstructions();
            if (instructions == null) {
                instructions = new ArrayList<>();
                pair.setInstructions(instructions);
            }
            removeTrailingBreak(instructions);
            Instruction result = results.get(i);
            if (result instanceof FastSwitch inner && nested.containsKey(inner)) {
                // The nested expression is already the last instruction of the case
                addCaseResults(inner, nested.get(inner), nested);
            } else if (result != null) {
                instructions.add(result);
            }
        }
    }

    private static int bodyOffset(FastSwitch fs, FastSwitch.Pair pair) {
        if (fs.getTest() instanceof PatternSwitchSelector selector && !pair.isDefault()) {
            return selector.getBodyOffsets().getOrDefault(pair.getKey(), pair.getOffset());
        }
        return pair.getOffset();
    }

    private static Instruction resolveCaseResultFromBytecode(ClassFile classFile, Method method, int startOffset, int breakOffset,
            int branchEnd) {
        byte[] code = method.getCode();
        if (code == null || startOffset < 0 || startOffset >= code.length) {
            return null;
        }

        LineNumberLookup lineNumbers = new LineNumberLookup(method);
        Deque<Instruction> stack = new ArrayDeque<>();
        List<Instruction> list = new ArrayList<>();
        List<Instruction> listForAnalyze = new ArrayList<>();
        boolean[] jumps = new boolean[code.length];

        int offset = startOffset;
        while (offset < code.length && offset < breakOffset) {
            int opcode = code[offset] & 255;

            if (opcode == Const.GOTO && isTernaryGoto(list, stack, offset)) {
                int target = offset + readBranchOffset(code, offset, opcode);
                int end = Math.min(target, branchEnd);
                list.add(GotoFactory.newTernaryOpStore(code, offset, end, stack.pop()));
                list.add(new Goto(opcode, offset, lineNumbers.getLineNumber(offset), target - offset));
                offset += Const.getNoOfOperands(opcode) + 1;
                continue;
            }

            if (opcode == Const.GOTO || opcode == Const.GOTO_W) {
                return reconstructTernaryOperators(list,
                        yieldedValue(stack, offset + readBranchOffset(code, offset, opcode), breakOffset));
            }

            InstructionFactory factory = getBranchInstructionFactory(opcode);
            if (factory == null) {
                return null;
            }

            int lineNumber = lineNumbers.getLineNumber(offset);
            try {
                offset += factory.create(classFile, method, list, listForAnalyze, stack, code, offset, lineNumber, jumps) + 1;
            } catch (NoSuchElementException e) {
                // The branch of a switch statement may use values computed before it, which are not known here
                return null;
            }
        }

        return reconstructTernaryOperators(list, stack.peek());
    }

    /** @return the factory of the instruction, null if a branch of a switch expression cannot be read through it */
    private static InstructionFactory getBranchInstructionFactory(int opcode) {
        return opcode == Const.TABLESWITCH || opcode == Const.LOOKUPSWITCH
                ? null : InstructionFactoryConstants.getInstructionFactory(opcode);
    }

    /** @return true if a condition jumps right after the jump, to the second value of a ternary operator */
    private static boolean isTernaryGoto(List<Instruction> list, Deque<Instruction> stack, int offset) {
        return !stack.isEmpty() && list.stream().anyMatch(instruction -> ByteCodeUtil.isIfInstruction(instruction.getOpcode(), false)
                && ((BranchInstruction) instruction).getJumpOffset() == offset + 3);
    }

    /** @return the value of the branch, made of the ternary operators ending with the one given, if any */
    private static Instruction reconstructTernaryOperators(List<Instruction> list, Instruction value) {
        if (value == null || list.stream().noneMatch(TernaryOpStore.class::isInstance)) {
            return value;
        }
        list.add(value);
        ComparisonInstructionAnalyzer.aggregate(list);
        TernaryOpReconstructor.reconstruct(list);
        return list.get(list.size() - 1);
    }

    /** @return the offset of the jump ending the branch starting at the given offset, or the end of the switch */
    private static int branchEnd(FastSwitch fs, int start) {
        int next = Stream.of(fs.getPairs()).mapToInt(pair -> bodyOffset(fs, pair)).filter(o -> o > start)
                .min().orElse(fs.getJumpOffset());
        // The branch ends with a jump of three bytes
        return next < fs.getJumpOffset() ? next - 3 : next;
    }

    /** @return the value left by a branch ending with a jump, if it jumps to the end of the switch */
    private static Instruction yieldedValue(Deque<Instruction> stack, int jumpOffset, int breakOffset) {
        return jumpOffset == breakOffset ? stack.peek() : null;
    }

    private static int readBranchOffset(byte[] code, int offset, int opcode) {
        if (opcode == Const.GOTO) {
            return (short)((code[offset + 1] & 255) << 8 | (code[offset + 2] & 255));
        }
        if (opcode == Const.GOTO_W) {
            return (code[offset + 1] << 24)
                    | ((code[offset + 2] & 255) << 16)
                    | ((code[offset + 3] & 255) << 8)
                    | (code[offset + 4] & 255);
        }
        return 0;
    }

    private static Instruction findLastNonBreakInstruction(List<Instruction> instructions) {
        if (instructions == null || instructions.isEmpty()) {
            return null;
        }
        int lastIndex = instructions.size() - 1;
        Instruction last = instructions.get(lastIndex);
        if (last instanceof FastInstruction fi && fi.getOpcode() == FastConstants.GOTO_BREAK) {
            lastIndex--;
        }
        return lastIndex >= 0 ? instructions.get(lastIndex) : null;
    }

    private static void removeTrailingBreak(List<Instruction> instructions) {
        if (instructions == null || instructions.isEmpty()) {
            return;
        }
        int lastIndex = instructions.size() - 1;
        Instruction last = instructions.get(lastIndex);
        if (last instanceof FastInstruction fi && fi.getOpcode() == FastConstants.GOTO_BREAK) {
            instructions.remove(lastIndex);
        }
    }

    private static void alignTargetLineNumber(FastSwitch fs, Instruction target) {
        int switchLine = fs.getLineNumber();
        if (switchLine == Instruction.UNKNOWN_LINE_NUMBER) {
            return;
        }
        int targetLine = target.getLineNumber();
        if (targetLine == Instruction.UNKNOWN_LINE_NUMBER || targetLine > switchLine) {
            target.setLineNumber(switchLine);
        }
    }

    private static int findNextTargetIndex(List<Instruction> list, int startIndex) {
        return IntStream.range(startIndex, list.size()).filter(i -> {
            Instruction target = unwrapTarget(list.get(i));
            return target != null && target.getOpcode() != FastConstants.GOTO_BREAK;
        }).findFirst().orElse(-1);
    }

    static List<List<Instruction>> getBlocks(Instruction instruction) {
        if (instruction instanceof FastTest2Lists fastTest2Lists) {
            List<List<Instruction>> blocks = new java.util.ArrayList<>(2);
            addBlock(blocks, fastTest2Lists.getInstructions());
            addBlock(blocks, fastTest2Lists.getInstructions2());
            return blocks;
        }
        if (instruction instanceof FastTestList fastTestList) {
            return toSingleBlock(fastTestList.getInstructions());
        }
        if (instruction instanceof FastTry fastTry) {
            List<List<Instruction>> blocks = new java.util.ArrayList<>();
            addBlock(blocks, fastTry.getInstructions());
            for (FastCatch fastCatch : fastTry.getCatches()) {
                addBlock(blocks, fastCatch.instructions());
            }
            if (fastTry.getFinallyInstructions() != null) {
                addBlock(blocks, fastTry.getFinallyInstructions());
            }
            return blocks;
        }
        if (instruction instanceof FastList fastList) {
            return toSingleBlock(fastList.getInstructions());
        }
        if (instruction instanceof FastFor fastFor) {
            return toSingleBlock(fastFor.getInstructions());
        }
        if (instruction instanceof FastForEach fastForEach) {
            return toSingleBlock(fastForEach.getInstructions());
        }
        if (instruction instanceof FastSwitch fastSwitch) {
            List<List<Instruction>> blocks = new java.util.ArrayList<>();
            for (FastSwitch.Pair pair : fastSwitch.getPairs()) {
                if (pair.getInstructions() != null) {
                    blocks.add(pair.getInstructions());
                }
            }
            return blocks;
        }
        if (instruction instanceof FastSynchronized fastSynchronized) {
            return toSingleBlock(fastSynchronized.getInstructions());
        }
        if (instruction instanceof FastLabel fastLabel && fastLabel.getInstruction() != null) {
            return getBlocks(fastLabel.getInstruction());
        }
        if (instruction instanceof FastDeclaration fastDeclaration && fastDeclaration.getInstruction() != null) {
            return getBlocks(fastDeclaration.getInstruction());
        }

        return List.of();
    }

    private static List<List<Instruction>> toSingleBlock(List<Instruction> instructions) {
        if (instructions == null) {
            return List.of();
        }
        return List.of(instructions);
    }

    private static void addBlock(List<List<Instruction>> blocks, List<Instruction> instructions) {
        if (instructions != null) {
            blocks.add(instructions);
        }
    }

    private static final class LineNumberLookup {
        private final LineNumber[] lineNumbers;
        private int index;
        private int currentLine;

        private LineNumberLookup(Method method) {
            this.lineNumbers = method.getLineNumbers();
            this.index = 0;
            this.currentLine = Instruction.UNKNOWN_LINE_NUMBER;
        }

        private int getLineNumber(int offset) {
            if (lineNumbers == null) {
                return Instruction.UNKNOWN_LINE_NUMBER;
            }
            while (index < lineNumbers.length && lineNumbers[index].getStartPC() <= offset) {
                currentLine = lineNumbers[index].getLineNumber();
                index++;
            }
            return currentLine;
        }
    }
}
