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
import jd.core.model.instruction.bytecode.instruction.attribute.ObjectrefAttribute;

public class Pop extends Instruction implements ObjectrefAttribute
{
    private Instruction objectref;

    public Pop(int opcode, int offset, int lineNumber, Instruction objectref)
    {
        super(opcode, offset, lineNumber);
        this.setObjectref(objectref);
    }

    @Override
    public String getReturnedSignature(
            ClassFile classFile, LocalVariables localVariables)
    {
        return null;
    }

    @Override
    public Instruction getObjectref() {
        return objectref;
    }

    @Override
    public void setObjectref(Instruction objectref) {
        this.objectref = objectref;
    }
}
