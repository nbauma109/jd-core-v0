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
package jd.core.model.instruction.bytecode.instruction;

import java.util.Optional;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.fast.FastConstants;

public class SwitchExpressionYield extends Instruction
{
    private Instruction value;

    public SwitchExpressionYield(int offset, int lineNumber, Instruction value)
    {
        super(FastConstants.SWITCH_EXPRESSION_YIELD, offset, lineNumber);
        this.value = value;
    }

    @Override
    public String getReturnedSignature(ClassFile classFile, LocalVariables localVariables)
    {
        return Optional.ofNullable(value)
                .map(val -> val.getReturnedSignature(classFile, localVariables))
                .orElse(null);
    }

    @Override
    public int getLastOffset()
    {
        return Optional.ofNullable(value)
                .map(Instruction::getLastOffset)
                .orElseGet(super::getLastOffset);
    }

    public Instruction getValue() {
        return value;
    }

    public void setValue(Instruction value) {
        this.value = value;
    }
}
