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
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;

public class LoadInstruction extends IndexInstruction
{
    private final String signature;

    public LoadInstruction(
        int opcode, int offset, int lineNumber, int index, String signature)
    {
        super(opcode, offset, lineNumber, index);
        this.signature = signature;
    }

    @Override
    public String getReturnedSignature(
            ClassFile classFile, LocalVariables localVariables)
    {
        return this.signature;
    }

    protected String getReturnedSignature(
            ClassFile classFile, LocalVariables localVariables, String defaultSignature) {

        if (classFile == null || localVariables == null) {
            return null;
        }

        ConstantPool constants = classFile.getConstantPool();

        LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(this.getIndex(), this.getOffset());

        if (lv == null || lv.getSignatureIndex() < 0) {
            return defaultSignature;
        }

        return lv.getSignature(constants);
    }
}
