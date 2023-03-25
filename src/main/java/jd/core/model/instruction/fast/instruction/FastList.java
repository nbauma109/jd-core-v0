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
package jd.core.model.instruction.fast.instruction;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.BranchInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;

/**
 * list & while(true)
 */
public class FastList extends BranchInstruction
{
    protected final List<Instruction> instructions;

    public FastList(
        int opcode, int offset, int lineNumber,
        int branch, List<Instruction> instructions)
    {
        super(opcode, offset, lineNumber, branch);
        this.instructions = instructions;
    }

    @Override
    public String getReturnedSignature(
            ClassFile classFile, LocalVariables localVariables)
    {
        return null;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public boolean isFirstInstructionInstanceOf(Class<? extends Instruction> clazz) {
        return Optional.ofNullable(instructions)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .findFirst()
                .filter(clazz::isInstance)
                .isPresent();
    }
}
