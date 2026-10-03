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
package jd.core.model.layout.block;

import org.apache.bcel.classfile.LineNumber;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.Instruction;

public class ByteCodeLayoutBlock extends LayoutBlock
{
    private final ClassFile classFile;
    private final Method method;

    public ByteCodeLayoutBlock(ClassFile classFile, Method method)
    {
        this(classFile, method, false);
    }

    public ByteCodeLayoutBlock(ClassFile classFile, Method method, boolean realignment)
    {
        super(
            LayoutBlockConstants.BYTE_CODE,
            Instruction.UNKNOWN_LINE_NUMBER, Instruction.UNKNOWN_LINE_NUMBER,
            0, 0, 0);
        this.classFile = classFile;
        this.method = method;
        if (realignment && method.getLineNumbers() != null && method.getLineNumbers().length > 0) {
            int first = Integer.MAX_VALUE;
            int last = Instruction.UNKNOWN_LINE_NUMBER;
            for (LineNumber line : method.getLineNumbers()) {
                if (line.getLineNumber() > 0) {
                    first = Math.min(first, line.getLineNumber());
                    last = Math.max(last, line.getLineNumber());
                }
            }
            if (last != Instruction.UNKNOWN_LINE_NUMBER) {
                setFirstLineNumber(first);
                setInstructionLineSpan(last);
            }
        }
    }

    public ClassFile getClassFile() {
        return classFile;
    }

    public Method getMethod() {
        return method;
    }
}
