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

import jd.core.model.instruction.bytecode.instruction.DupLoad;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.instruction.fast.visitor.AbstractReplaceInstructionVisitor;

public class ReplaceDupLoadVisitor extends AbstractReplaceInstructionVisitor<Instruction>
{
    private DupStore dupStore;

    public ReplaceDupLoadVisitor() {
    }

    public ReplaceDupLoadVisitor(DupStore dupStore, Instruction newInstruction)
    {
        init(dupStore, newInstruction);
    }

    public void init(DupStore dupStore, Instruction newInstruction)
    {
        super.init(newInstruction);
        this.dupStore = dupStore;
    }

    @Override
    protected Instruction match(Instruction i)
    {
        if (i instanceof DupLoad dl && dl.getDupStore() == dupStore)
        {
            return i;
        }
        return null;
    }
}
