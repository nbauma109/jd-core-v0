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

import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.Signature;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.LocalVariables;

public class GetField extends IndexInstruction
{
    private Instruction objectref;

    public GetField(
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

        ConstantFieldref cfr = constants.getConstantFieldref(this.getIndex());
        ConstantNameAndType cnat =
            constants.getConstantNameAndType(cfr.getNameAndTypeIndex());

        String fieldName = constants.getConstantUtf8(cnat.getNameIndex());
        String fieldSignature = constants.getConstantUtf8(cnat.getSignatureIndex());
        Field field = classFile.getField(fieldName, fieldSignature);
        if (field != null) {
            Signature attributeSignature = field.getAttributeSignature();
            if (attributeSignature != null) {
                return attributeSignature.getSignature();
            }
        }
        return constants.getConstantUtf8(cnat.getSignatureIndex());
    }

    public Instruction getObjectref() {
        return objectref;
    }

    public void setObjectref(Instruction objectref) {
        this.objectref = objectref;
    }
}
