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

import jd.core.model.instruction.bytecode.instruction.attribute.ValuerefAttribute;

public class StoreInstruction
    extends LoadInstruction implements ValuerefAttribute
{
    private Instruction valueref;
    private StoreInstruction next;

    public StoreInstruction(
            int opcode, int offset, int lineNumber, int index,
            String signature, Instruction valueref)
    {
        super(opcode, offset, lineNumber, index, signature);
        this.setValueref(valueref);
    }

    @Override
    public Instruction getValueref() {
        return valueref;
    }

    public void setValueref(Instruction valueref) {
        this.valueref = valueref;
    }

    public StoreInstruction getNext() {
        return next;
    }

    public void setNext(StoreInstruction next) {
        this.next = next;
    }

    @Override
    public int getOffset() {
        return next == null ? super.getOffset() : next.getOffset();
    }
}
