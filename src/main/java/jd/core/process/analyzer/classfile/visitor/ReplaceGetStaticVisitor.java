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

import org.apache.bcel.Const;

import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.instruction.fast.visitor.AbstractReplaceInstructionVisitor;

public class ReplaceGetStaticVisitor extends AbstractReplaceInstructionVisitor<Instruction>
{
    private final int index;

    public ReplaceGetStaticVisitor(int index, Instruction newInstruction)
    {
        super(newInstruction);
        this.index = index;
    }

    @Override
    protected Instruction match(Instruction i)
    {
        if (i.getOpcode() == Const.GETSTATIC &&
            ((GetStatic)i).getIndex() == this.index)
        {
            return i;
        }
        return null;
    }
}
