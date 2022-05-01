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

import org.apache.bcel.Const;
import org.apache.bcel.classfile.Constant;
import org.jd.core.v1.util.StringConstants;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;

public class Ldc extends LdcInstruction
{
    public Ldc(int opcode, int offset, int lineNumber, int index)
    {
        super(opcode, offset, lineNumber, index);
    }

    @Override
    public String getReturnedSignature(
            ConstantPool constants, LocalVariables localVariables)
    {
        if (constants == null) {
            return null;
        }

        Constant c = constants.get(this.getIndex());

        if (c == null) {
            return null;
        }

        return switch (c.getTag())
        {
        case Const.CONSTANT_Float   -> "F";
        case Const.CONSTANT_Integer -> "I";
        case Const.CONSTANT_String  -> StringConstants.INTERNAL_STRING_SIGNATURE;
        case Const.CONSTANT_Class   -> StringConstants.INTERNAL_CLASS_SIGNATURE;
            /*{
                int index = ((ConstantClass)c).nameIndex;
                return SignatureUtil.CreateTypeName(
                    constants.getConstantUtf8(index));
            }*/
        default -> null;
        };
    }
}
