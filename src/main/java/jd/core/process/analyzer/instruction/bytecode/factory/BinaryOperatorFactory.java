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
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class BinaryOperatorFactory implements InstructionFactory
{
    protected final int priority;
    protected final String signature;
    protected final String operator;

    public BinaryOperatorFactory(
            int priority, String signature, String operator)
    {
        this.priority = priority;
        this.signature = signature;
        this.operator = operator;
    }

    @Override
    public int create(
            ClassFile classFile, Method method, List<Instruction> list,
            List<Instruction> listForAnalyze,
            Deque<Instruction> stack, byte[] code, int offset,
            int lineNumber, boolean[] jumps)
    {
        final int opcode = code[offset] & 255;
        final Instruction i2 = stack.pop();
        final Instruction i1 = stack.pop();

        final Instruction instruction = new BinaryOperatorInstruction(
            ByteCodeConstants.BINARYOP, offset, lineNumber, this.priority,
            this.signature, this.operator, i1, i2);

        stack.push(instruction);
        listForAnalyze.add(instruction);

        return Const.getNoOfOperands(opcode);
    }
}
