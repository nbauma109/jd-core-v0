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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.LineNumber;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.PatternSwitchSelector;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
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
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactory;
import jd.core.process.analyzer.instruction.bytecode.factory.InstructionFactoryConstants;
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
        List<Instruction> results = resolveCaseResults(classFile, method, fs);
        // The instructions following the switch consume the value of the last case
        Instruction consumed = results == null ? null
                : results.stream().filter(Objects::nonNull).max(Comparator.comparingInt(Instruction::getOffset)).orElse(null);
        if (consumed == null) {
            return false;
        }

        if (target instanceof StoreInstruction si && si.getValueref().getOffset() == consumed.getOffset()) {
            forceBooleanSignature(si.getReturnedSignature(classFile, method.getLocalVariables()), results);
            si.setValueref(fs);
        } else if (target instanceof ReturnInstruction ri && ri.getValueref().getOffset() == consumed.getOffset()) {
            forceBooleanSignature(ri.getReturnedSignature(classFile, method.getLocalVariables()), results);
            ri.setValueref(fs);
        } else {
            ReplaceInstructionVisitor visitor = new ReplaceInstructionVisitor(consumed.getOffset(), fs);
            visitor.visit(target);
            if (visitor.getOldInstruction() == null) {
                return false;
            }
        }

        addCaseResults(fs, results);
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

    /** @return the value of each case, null for the cases throwing, or null if the switch is not an expression */
    private static List<Instruction> resolveCaseResults(ClassFile classFile, Method method, FastSwitch fs) {
        FastSwitch.Pair[] pairs = fs.getPairs();
        if (pairs == null || pairs.length == 0) {
            return null;
        }

        int breakOffset = fs.getJumpOffset();
        List<Instruction> results = new ArrayList<>(pairs.length);

        for (FastSwitch.Pair pair : pairs) {
            Instruction last = findLastNonBreakInstruction(pair.getInstructions());
            if (last != null && last.getOpcode() == Const.ATHROW) {
                results.add(null);
                continue;
            }

            Instruction result = resolveCaseResultFromBytecode(classFile, method, bodyOffset(fs, pair), breakOffset);
            if (result == null) {
                return null;
            }
            results.add(result);
        }

        return results;
    }

    private static void forceBooleanSignature(String targetSignature, List<Instruction> results) {
        if (targetSignature != null && targetSignature.startsWith("Z")) {
            for (Instruction result : results) {
                if (result instanceof IConst iConst) {
                    iConst.setSignature("Z");
                }
            }
        }
    }

    private static void addCaseResults(FastSwitch fs, List<Instruction> results) {
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
            if (result != null) {
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

    private static Instruction resolveCaseResultFromBytecode(ClassFile classFile, Method method, int startOffset, int breakOffset) {
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

            if (opcode == Const.GOTO || opcode == Const.GOTO_W) {
                int jumpOffset = offset + readBranchOffset(code, offset, opcode);
                if (jumpOffset == breakOffset) {
                    return stack.peek();
                }
                return null;
            }

            if (opcode == Const.TABLESWITCH || opcode == Const.LOOKUPSWITCH) {
                return null;
            }

            InstructionFactory factory = InstructionFactoryConstants.getInstructionFactory(opcode);
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

        return stack.peek();
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
        for (int i = startIndex; i < list.size(); i++) {
            Instruction candidate = list.get(i);
            Instruction unwrapped = unwrapTarget(candidate);
            if (candidate instanceof FastLabel && !(unwrapped instanceof StoreInstruction)
                    && !(unwrapped instanceof ReturnInstruction)
                    || unwrapped instanceof FastInstruction fi && fi.getOpcode() == FastConstants.GOTO_BREAK) {
                continue;
            }
            return unwrapped == null ? -1 : i;
        }
        return -1;
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
