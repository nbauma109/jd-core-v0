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
package jd.core.process.analyzer.classfile.visitor;

import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.instruction.fast.visitor.AbstractReplaceInstructionVisitor;

/*
 * Replace 'ALoad(1)' in constructor by 'OuterThis()':
 * replace '???.xxx' by 'TestInnerClass.this.xxx'.
 */
public class ReplaceOuterReferenceVisitor extends AbstractReplaceInstructionVisitor<Instruction>
{
    private final int opcode;
    private final int index;
    private final int outerThisInstructionIndex;

    public ReplaceOuterReferenceVisitor(
        int opcode, int index, int outerThisInstructionIndex)
    {
        this.opcode = opcode;
        this.index = index;
        this.outerThisInstructionIndex = outerThisInstructionIndex;
    }

    @Override
    protected Instruction match(Instruction parentFound, Instruction instruction)
    {
        return match(instruction);
    }

    @Override
    protected Instruction match(Instruction i)
    {
        if (i.getOpcode() == this.opcode &&
            ((IndexInstruction)i).getIndex() == this.index)
        {
            return i;
        }
        return null;
    }

    @Override
    protected Instruction newInstruction(Instruction i, Instruction item)
    {
        return new GetStatic(
            ByteCodeConstants.OUTERTHIS, i.getOffset(),
            i.getLineNumber(), this.outerThisInstructionIndex);
    }
}
