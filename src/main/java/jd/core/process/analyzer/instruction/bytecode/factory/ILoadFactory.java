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

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.IInc;
import jd.core.model.instruction.bytecode.instruction.ILoad;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class ILoadFactory implements InstructionFactory
{
    @Override
    public int create(
            ClassFile classFile, Method method, List<Instruction> list,
            List<Instruction> listForAnalyze,
            Deque<Instruction> stack, byte[] code, int offset,
            int lineNumber, boolean[] jumps)
    {
        final int opcode = code[offset] & 255;
        int index;

        if (opcode == Const.ILOAD) {
            index = code[offset+1] & 255;
        } else {
            index = opcode - Const.ILOAD_0;
        }

        Instruction instruction =
            new ILoad(Const.ILOAD, offset, lineNumber, index);

        if (!stack.isEmpty() && !jumps[offset])
        {
            Instruction last = stack.peek();

            if (last.getOpcode() == Const.IINC)
            {
                if (((IInc)last).getIndex() == index)
                {
                    listForAnalyze.add(instruction);

                    // Replace temporary IInc instruction by a pre-inc instruction
                    IInc iinc = (IInc)last;
                    stack.pop();
                    instruction = new IncInstruction(
                            ByteCodeConstants.PREINC, iinc.getOffset(),
                            iinc.getLineNumber(), instruction, iinc.getCount());
                }
                else
                {
                    // Unknown pattern. Move IInc instruction from stack to list.
                    stack.pop();
                    list.add(last);
                    listForAnalyze.add(last);
                }
            }
        }

        stack.push(instruction);

        listForAnalyze.add(instruction);

        return Const.getNoOfOperands(opcode);
    }
}
