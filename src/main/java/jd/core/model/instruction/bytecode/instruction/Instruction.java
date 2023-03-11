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

public abstract class Instruction
{
    public static final int UNKNOWN_LINE_NUMBER = 0;
    public static final int ZERO_PRIORITY = 0;

    private int opcode;
    private final int offset;
    private int lineNumber;
    private Instruction next; // comma-separated instruction in for and declarations
    private boolean hidden;
    
    protected Instruction(int opcode, int offset, int lineNumber)
    {
        this.opcode = opcode;
        this.offset = offset;
        this.setLineNumber(lineNumber);
    }

    public abstract String getReturnedSignature(
            ClassFile classFile, LocalVariables localVariables);

    public int getPriority()
    {
        return ZERO_PRIORITY;
    }

    public int getOffset() {
        return offset;
    }

    public int getLastOffset() {
        return next == null ? offset : next.getLastOffset();
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getOpcode() {
        return opcode;
    }

    public void setOpcode(int opcode) {
        this.opcode = opcode;
    }

    public Instruction getNext() {
        return next;
    }

    public void setNext(Instruction next) {
        this.next = next;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean holdsLambda() {
        return false;
    }
}
