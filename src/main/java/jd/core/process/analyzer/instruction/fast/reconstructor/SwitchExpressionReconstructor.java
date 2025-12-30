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
package jd.core.process.analyzer.instruction.fast.reconstructor;

import org.apache.bcel.Const;

import java.util.List;

import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.model.instruction.bytecode.instruction.SwitchExpression;
import jd.core.model.instruction.bytecode.instruction.SwitchExpressionYield;
import jd.core.model.instruction.fast.FastConstants;
import jd.core.model.instruction.fast.instruction.FastDeclaration;
import jd.core.model.instruction.fast.instruction.FastSwitch;

public final class SwitchExpressionReconstructor
{
    private SwitchExpressionReconstructor() {
    }

    public static void reconstruct(List<Instruction> list)
    {
        for (int index = 0; index < list.size() - 1; index++)
        {
            Instruction instruction = list.get(index);
            if (!(instruction instanceof FastSwitch fs)) {
                continue;
            }

            Instruction nextInstruction = list.get(index + 1);
            StoreInstruction store = null;
            ReturnInstruction ret = null;

            if (nextInstruction instanceof FastDeclaration fd) {
                if (fd.getInstruction() instanceof StoreInstruction si) {
                    store = si;
                } else {
                    continue;
                }
            } else if (nextInstruction instanceof StoreInstruction si) {
                store = si;
            } else if (nextInstruction instanceof ReturnInstruction ri) {
                ret = ri;
            }

            if (store == null && ret == null) {
                continue;
            }

            if (!isSwitchExpressionCandidate(fs)) {
                continue;
            }

            prepareSwitchExpression(fs);

            int offset = fs.getTest().getOffset();
            SwitchExpression expression = new SwitchExpression(offset, fs.getLineNumber(), fs);

            if (store != null) {
                store.setValueref(expression);
            } else {
                ret.setValueref(expression);
            }

            list.remove(index);
            index--;
        }
    }

    private static boolean isSwitchExpressionCandidate(FastSwitch fs)
    {
        FastSwitch.Pair[] pairs = fs.getPairs();
        if (pairs == null || pairs.length == 0) {
            return false;
        }

        for (FastSwitch.Pair pair : pairs) {
            if (pair == null) {
                continue;
            }
            List<Instruction> instructions = pair.getInstructions();
            if (instructions == null || instructions.isEmpty()) {
                return false;
            }

            Instruction lastInstruction = instructions.get(instructions.size() - 1);
            if (lastInstruction.getOpcode() == FastConstants.GOTO_BREAK) {
                if (instructions.size() == 1) {
                    return false;
                }
                continue;
            }

            if (lastInstruction.getOpcode() == Const.ATHROW) {
                continue;
            }

            return false;
        }

        return true;
    }

    private static void prepareSwitchExpression(FastSwitch fs)
    {
        fs.setSwitchExpression(true);

        FastSwitch.Pair[] pairs = fs.getPairs();
        for (FastSwitch.Pair pair : pairs) {
            if (pair == null) {
                continue;
            }
            List<Instruction> instructions = pair.getInstructions();
            if (instructions == null || instructions.isEmpty()) {
                continue;
            }

            Instruction lastInstruction = instructions.get(instructions.size() - 1);
            if (lastInstruction.getOpcode() == FastConstants.GOTO_BREAK) {
                instructions.remove(instructions.size() - 1);
                if (instructions.isEmpty()) {
                    continue;
                }
                lastInstruction = instructions.get(instructions.size() - 1);
            }

            if (instructions.size() > 1 && lastInstruction.getOpcode() != Const.ATHROW) {
                SwitchExpressionYield yield = new SwitchExpressionYield(
                        lastInstruction.getOffset(),
                        lastInstruction.getLineNumber(),
                        lastInstruction);
                instructions.set(instructions.size() - 1, yield);
            }
        }
    }
}
