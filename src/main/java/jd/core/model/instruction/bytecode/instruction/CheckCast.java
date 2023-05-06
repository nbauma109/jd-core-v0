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

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantUtf8;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.attribute.ObjectrefAttribute;
import jd.core.util.SignatureUtil;

public class CheckCast extends IndexInstruction implements ObjectrefAttribute
{
    private Instruction objectref;

    public CheckCast(
        int opcode, int offset, int lineNumber,
        int index, Instruction objectref)
    {
        super(opcode, offset, lineNumber, index);
        this.setObjectref(objectref);
    }

    @Override
    public String getReturnedSignature(
            ClassFile classFile, LocalVariables localVariables)
    {
        if (classFile == null) {
            return null;
        }

        ConstantPool constants = classFile.getConstantPool();

        Constant c = constants.get(this.getIndex());

        if (c instanceof ConstantUtf8 cutf8)
        {
            return cutf8.getBytes();
        }
        ConstantClass cc = (ConstantClass)c;
        String signature = constants.getConstantUtf8(cc.getNameIndex());
        if (signature.charAt(0) != '[') {
            signature = SignatureUtil.createTypeName(signature);
        }
        return signature;
    }

    @Override
    public int getPriority()
    {
        return 2;
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
