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
import java.util.Deque;
import java.util.List;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.LineNumber;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.Instruction;
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

public final class SwitchExpressionReconstructor {

    private SwitchExpressionReconstructor() {
    }

    public static void reconstruct(ClassFile classFile, Method method, List<Instruction> list) {
        if (list == null || list.isEmpty() || classFile == null || method == null) {
            return;
        }

        for (int index = 0; index < list.size(); index++) {
            Instruction instruction = list.get(index);

            if (instruction instanceof FastSwitch fs) {
                int targetIndex = findNextTargetIndex(list, index + 1);
                if (targetIndex != -1) {
                    Instruction targetInstruction = list.get(targetIndex);
                    if (tryReplaceWithSwitchExpression(classFile, method, fs, targetInstruction)) {
                        list.remove(index);
                        index--;
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
        if ((!(target instanceof StoreInstruction) && !(target instanceof ReturnInstruction))) {
            return false;
        }

        String targetSignature = getTargetSignature(classFile, method, target);
        if (!populateSwitchExpressionCases(classFile, method, fs, targetSignature)) {
            return false;
        }

        alignTargetLineNumber(fs, targetInstruction);
        if (target != targetInstruction) {
            alignTargetLineNumber(fs, target);
        }

        if (target instanceof StoreInstruction si) {
            si.setValueref(fs);
        } else {
            ((ReturnInstruction) target).setValueref(fs);
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

    private static String getTargetSignature(ClassFile classFile, Method method, Instruction target) {
        if (target instanceof ReturnInstruction ri) {
            return ri.getReturnedSignature(classFile, method.getLocalVariables());
        }
        if (target instanceof StoreInstruction si) {
            return si.getReturnedSignature(classFile, method.getLocalVariables());
        }
        return null;
    }

    private static boolean populateSwitchExpressionCases(ClassFile classFile, Method method, FastSwitch fs, String targetSignature) {
        FastSwitch.Pair[] pairs = fs.getPairs();
        if (pairs == null || pairs.length == 0) {
            return false;
        }

        int breakOffset = fs.getJumpOffset();
        List<Instruction> resolvedResults = new ArrayList<>(pairs.length);

        for (FastSwitch.Pair pair : pairs) {
            List<Instruction> instructions = pair.getInstructions();
            Instruction last = findLastNonBreakInstruction(instructions);
            if (last != null && last.getOpcode() == Const.ATHROW) {
                resolvedResults.add(null);
                continue;
            }

            Instruction result = resolveCaseResultFromBytecode(classFile, method, pair.getOffset(), breakOffset);
            if (result == null) {
                return false;
            }
            if (shouldForceBooleanSignature(targetSignature, result)) {
                ((jd.core.model.instruction.bytecode.instruction.IConst) result).setSignature("Z");
            }
            resolvedResults.add(result);
        }

        for (int i = 0; i < pairs.length; i++) {
            FastSwitch.Pair pair = pairs[i];
            List<Instruction> instructions = pair.getInstructions();
            if (instructions == null) {
                instructions = new ArrayList<>();
                pair.setInstructions(instructions);
            }
            removeTrailingBreak(instructions);
            Instruction result = resolvedResults.get(i);
            if (result != null) {
                instructions.add(result);
            }
        }

        return true;
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
            int consumed = factory.create(classFile, method, list, listForAnalyze, stack, code, offset, lineNumber, jumps);
            offset += consumed + 1;
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

    private static boolean shouldForceBooleanSignature(String targetSignature, Instruction result) {
        if (targetSignature == null || targetSignature.isEmpty()) {
            return false;
        }
        if (targetSignature.charAt(0) != 'Z') {
            return false;
        }
        return result instanceof jd.core.model.instruction.bytecode.instruction.IConst;
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
            if (unwrapped instanceof StoreInstruction || unwrapped instanceof ReturnInstruction) {
                return i;
            }
            if (unwrapped == null) {
                continue;
            }
            if (unwrapped instanceof FastInstruction fi && fi.getOpcode() == FastConstants.GOTO_BREAK) {
                continue;
            }
            if (candidate instanceof FastLabel) {
                continue;
            }
            return -1;
        }
        return -1;
    }

    private static List<List<Instruction>> getBlocks(Instruction instruction) {
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
