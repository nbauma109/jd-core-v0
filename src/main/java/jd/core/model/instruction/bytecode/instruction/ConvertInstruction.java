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

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.LocalVariables;

public class ConvertInstruction extends Instruction
{
    private final String signature;
    private Instruction value;

    public ConvertInstruction(
            int opcode, int offset, int lineNumber, Instruction value, String signature)
    {
        super(opcode, offset, lineNumber);
        this.setValue(value);
        this.signature = signature;
    }

    @Override
    public String getReturnedSignature(
            ClassFile classFile, LocalVariables localVariables)
    {
        return this.getSignature();
    }

    @Override
    public int getPriority()
    {
        return 2;
    }

    public String getSignature() {
        return signature;
    }

    public Instruction getValue() {
        return value;
    }

    public void setValue(Instruction value) {
        this.value = value;
    }
}
