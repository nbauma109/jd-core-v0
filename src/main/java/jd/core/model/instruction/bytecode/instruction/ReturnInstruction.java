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
import jd.core.model.instruction.bytecode.instruction.attribute.ValuerefAttribute;

public class ReturnInstruction extends Instruction implements ValuerefAttribute
{
    private Instruction valueref;
    private boolean keywordPrinted;
    private final String signature;

    public ReturnInstruction(
        int opcode, int offset, int lineNumber, Instruction valueref, String signature)
    {
        super(opcode, offset, lineNumber);
        this.setValueref(valueref);
        this.signature = signature;
    }

    @Override
    public String getReturnedSignature(
            ClassFile classFile, LocalVariables localVariables)
    {
        return signature;
    }

    @Override
    public Instruction getValueref() {
        return valueref;
    }

    @Override
    public void setValueref(Instruction valueref) {
        this.valueref = valueref;
        if (valueref instanceof IConst) {
            IConst iConst = (IConst) valueref;
            iConst.setSignature(signature);
        }
    }

    public boolean isKeywordPrinted() {
        return keywordPrinted;
    }

    public void setKeywordPrinted(boolean keywordPrinted) {
        this.keywordPrinted = keywordPrinted;
    }
}
