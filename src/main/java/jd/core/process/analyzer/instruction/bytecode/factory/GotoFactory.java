/*******************************************************************************
 * Copyright (C) 2007-2019 Emmanuel Dupuy GPLv3
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
package jd.core.process.analyzer.instruction.bytecode.factory;

import org.apache.bcel.Const;

import java.util.Deque;
import java.util.List;
import java.util.stream.IntStream;

import static jd.core.model.instruction.bytecode.ByteCodeConstants.ARRAYLOAD;
import static jd.core.model.instruction.bytecode.ByteCodeConstants.ASSIGNMENT;
import static jd.core.model.instruction.bytecode.ByteCodeConstants.DCONST;
import static jd.core.model.instruction.bytecode.ByteCodeConstants.DUPLOAD;
import static jd.core.model.instruction.bytecode.ByteCodeConstants.EXCEPTIONLOAD;
import static jd.core.model.instruction.bytecode.ByteCodeConstants.ICONST;
import static jd.core.model.instruction.bytecode.ByteCodeConstants.INVOKENEW;
import static jd.core.model.instruction.bytecode.ByteCodeConstants.LOAD;
import static jd.core.model.instruction.bytecode.ByteCodeConstants.OUTERTHIS;
import static jd.core.model.instruction.bytecode.ByteCodeConstants.POSTINC;
import static org.apache.bcel.Const.ACONST_NULL;
import static org.apache.bcel.Const.ARRAYLENGTH;
import static org.apache.bcel.Const.CHECKCAST;
import static org.apache.bcel.Const.DCMPG;
import static org.apache.bcel.Const.DUP;
import static org.apache.bcel.Const.GETFIELD;
import static org.apache.bcel.Const.GETSTATIC;
import static org.apache.bcel.Const.IASTORE;
import static org.apache.bcel.Const.INSTANCEOF;
import static org.apache.bcel.Const.INVOKEINTERFACE;
import static org.apache.bcel.Const.INVOKEVIRTUAL;
import static org.apache.bcel.Const.MULTIANEWARRAY;
import static org.apache.bcel.Const.NEW;
import static org.apache.bcel.Const.SALOAD;
import static org.apache.bcel.Const.SASTORE;
import static org.apache.bcel.Const.WIDE;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.BranchInstruction;
import jd.core.model.instruction.bytecode.instruction.Goto;
import jd.core.model.instruction.bytecode.instruction.PatternSwitchSelector;
import jd.core.model.instruction.bytecode.instruction.Switch;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.TernaryOpStore;
import jd.core.process.analyzer.instruction.bytecode.util.ByteCodeUtil;

public class GotoFactory implements InstructionFactory
{
    @Override
    public int create(
            ClassFile classFile, Method method, List<Instruction> list,
            List<Instruction> listForAnalyze,
            Deque<Instruction> stack, byte[] code, int offset,
            int lineNumber, boolean[] jumps)
    {
        final int opcode = code[offset] & 255;
        final int value  =
            (short)((code[offset+1] & 255) << 8 | code[offset+2] & 255);

        // Only a switch expression whose value is stored or returned can be reconstructed
        boolean assignsOrReturns = isStoreOrReturn(code, offset + value);

        if (!stack.isEmpty() && !list.isEmpty()
                && !(assignsOrReturns && startsSwitchBranch(list, offset + 3)
                        && isFollowedByThrowingBranchOnly(code, offset, value))) {
            generateTernaryOpStore(
                list, listForAnalyze, stack, code, offset, value, assignsOrReturns);
        }

        list.add(new Goto(opcode, offset, lineNumber, value));

        return Const.getNoOfOperands(opcode);
    }

    /**
     * Javac lays out "default -> throw ..." as the last block of a switch expression: nothing falls through to the end of
     * the switch, so the value pushed before the last jump is the one the following instructions consume.
     */
    private static boolean isFollowedByThrowingBranchOnly(byte[] code, int offset, int value)
    {
        int target = offset + value;
        int lastOpcode = Const.NOP;

        for (int pos = offset + 3; pos < target && pos < code.length; pos = followingOffset(code, pos))
        {
            lastOpcode = code[pos] & 255;

            if (isBranchTo(code, pos, target)) {
                return false;
            }
        }

        return lastOpcode == Const.ATHROW;
    }

    private static int followingOffset(byte[] code, int pos)
    {
        int opcode = code[pos] & 255;
        int next = ByteCodeUtil.nextInstructionOffset(code, pos);

        // For variable length instructions, the offset of their last byte is returned
        return Const.getNoOfOperands(opcode) == Const.UNPREDICTABLE ? next + 1 : next;
    }

    private static boolean startsSwitchBranch(List<Instruction> list, int branchOffset)
    {
        for (Instruction instruction : list)
        {
            if (instruction instanceof Switch sw
                    && (sw.getOffset() + sw.getDefaultOffset() == branchOffset
                        || IntStream.of(sw.getOffsets()).anyMatch(o -> sw.getOffset() + o == branchOffset))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStoreOrReturn(byte[] code, int pos)
    {
        if (pos < 0 || pos >= code.length) {
            return false;
        }
        int opcode = code[pos] & 255;
        if (opcode == Const.WIDE && pos + 1 < code.length) {
            opcode = code[pos + 1] & 255;
        }
        return opcode >= Const.ISTORE && opcode <= Const.ASTORE_3
            || opcode >= Const.IRETURN && opcode <= Const.ARETURN;
    }

    private static boolean isBranchTo(byte[] code, int pos, int target)
    {
        int opcode = code[pos] & 255;

        if (opcode != Const.GOTO && !ByteCodeUtil.isIfInstruction(opcode, true)) {
            return false;
        }

        return pos + (short) ((code[pos + 1] & 255) << 8 | code[pos + 2] & 255) == target;
    }

    private static void generateTernaryOpStore(
        List<Instruction> list, List<Instruction> listForAnalyze,
        Deque<Instruction> stack, byte[] code, int offset, int value, boolean assignsOrReturns)
    {
        int i = list.size();

        while (i-- > 0)
        {
            Instruction previousInstruction = list.get(i);

            if (previousInstruction instanceof Switch sw
                    && (assignsOrReturns || sw.getKey() instanceof PatternSwitchSelector))
            {
                // The values left on the stack belong to the cases of a switch expression
                return;
            }

            Switch active = activeSwitch(list);
            if (ByteCodeUtil.isIfInstruction(previousInstruction.getOpcode(), false)
                && !(active != null && skipsBackwardJump(code, (BranchInstruction) previousInstruction, active)))
            {
                // Gestion de l'operateur ternaire
                final int ternaryOp2ndValueOffset =
                    search2ndValueOffset(code, offset, offset+value);

                final Instruction value0 = stack.pop();
                TernaryOpStore tos = new TernaryOpStore(
                    ByteCodeConstants.TERNARYOPSTORE, offset-1,
                    value0.getLineNumber(), value0, ternaryOp2ndValueOffset);

                list.add(tos);
                listForAnalyze.add(tos);
                return;
            }
        }
    }

    /** The selector of the last switch on patterns, whose cases may be restarted. */
    private static Switch activeSwitch(List<Instruction> list)
    {
        Switch active = null;
        for (Instruction instruction : list)
        {
            if (instruction instanceof Switch sw && sw.getKey() instanceof PatternSwitchSelector) {
                active = sw;
            }
        }
        return active;
    }

    /**
     * A condition jumping to a block restarting the matching of a switch on patterns (set the index,
     * jump back), or jumping over such a block, is not the condition of a ternary operator.
     */
    private static boolean skipsBackwardJump(byte[] code, BranchInstruction branch, Switch active)
    {
        return isRestartBlock(code, branch.getJumpOffset(), active)
            || branch.getBranch() > 0 && isRestartBlock(code, branch.getOffset() + 3, active);
    }

    /** The block sets the variable holding the index the matching restarts from, then jumps back to the switch. */
    private static boolean isRestartBlock(byte[] code, int start, Switch active)
    {
        int restartVariable = ((PatternSwitchSelector) active.getKey()).getRestartIndexVariable();
        int pos = start;
        boolean setsRestartIndex = false;
        for (int i = 0; i < 3 && pos + 2 < code.length; i++)
        {
            int opcode = code[pos] & 255;
            if (opcode == Const.GOTO)
            {
                return setsRestartIndex
                    && pos + (short) ((code[pos + 1] & 255) << 8 | code[pos + 2] & 255) <= active.getOffset();
            }
            int stored = storedVariable(code, pos);
            if (stored >= 0)
            {
                setsRestartIndex = stored == restartVariable;
            }
            else if (!isConstantPush(opcode))
            {
                return false;
            }
            pos += instructionLength(opcode);
        }
        return false;
    }

    private static boolean isConstantPush(int opcode)
    {
        return opcode >= Const.ICONST_M1 && opcode <= Const.ICONST_5 || opcode == Const.BIPUSH;
    }

    /** @return the variable stored by the int store at the position, or -1 */
    private static int storedVariable(byte[] code, int pos)
    {
        int opcode = code[pos] & 255;
        if (opcode >= Const.ISTORE_0 && opcode <= Const.ISTORE_3)
        {
            return opcode - Const.ISTORE_0;
        }
        return opcode == Const.ISTORE ? code[pos + 1] & 255 : -1;
    }

    private static int instructionLength(int opcode)
    {
        return opcode == Const.BIPUSH || opcode == Const.ISTORE ? 2 : 1;
    }

    private static int search2ndValueOffset(
            byte[] code, int offset, int jumpOffset)
    {
        int result = offset;

        while (offset < jumpOffset)
        {
            int opcode = code[offset] & 255;
            // on retient l'offset de la derniere opération placant une
            // information sur la pile.
            if (opcode >= ACONST_NULL   && opcode <= SALOAD          // 1..53
             || opcode >= IASTORE       && opcode <= SASTORE         // 79..86
             || opcode >= DUP           && opcode <= DCMPG           // 89..152
             || opcode == GETSTATIC                                  // 178
             || opcode == GETFIELD                                   // 180
             || opcode >= INVOKEVIRTUAL && opcode <= INVOKEINTERFACE // 182..185
             || opcode >= NEW           && opcode <= ARRAYLENGTH     // 187..190
             || opcode >= CHECKCAST     && opcode <= INSTANCEOF      // 192..193
             || opcode >= WIDE          && opcode <= MULTIANEWARRAY  // 196..197
             || opcode >= ICONST        && opcode <= DCONST          // 256..259
             || opcode == DUPLOAD                                    // 263
             || opcode >= ASSIGNMENT    && opcode <= LOAD            // 265..268
             || opcode >= EXCEPTIONLOAD && opcode <= ARRAYLOAD       // 270..271
             || opcode >= INVOKENEW     && opcode <= POSTINC         // 274..278
             || opcode == OUTERTHIS                                  // 285
            )
            {
                if (opcode == Const.DUP_X1)
                {
                    result = offset - 1;
                } else
                {
                    result = offset;
                }
            }

            int nbOfOperands = Const.getNoOfOperands(opcode);

            switch (nbOfOperands)
            {
            case Const.UNPREDICTABLE:
                switch (opcode)
                {
                case Const.TABLESWITCH:
                    offset = ByteCodeUtil.nextTableSwitchOffset(code, offset);
                    break;
                case Const.LOOKUPSWITCH:
                    offset = ByteCodeUtil.nextLookupSwitchOffset(code, offset);
                    break;
                case Const.WIDE:
                    offset = ByteCodeUtil.nextWideOffset(code, offset);
                }
                break;
            case Const.UNDEFINED:
                break;
            default:
                offset += nbOfOperands;
            }

            ++offset;
        }

        return result;
    }
}
