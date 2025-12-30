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
import jd.core.model.instruction.fast.FastConstants;
import jd.core.model.instruction.fast.instruction.FastSwitch;

public class SwitchExpression extends Instruction
{
    private final FastSwitch fs;

    public SwitchExpression(int offset, int lineNumber, FastSwitch fs)
    {
        super(FastConstants.SWITCH_EXPRESSION, offset, lineNumber);
        this.fs = fs;
    }

    @Override
    public String getReturnedSignature(ClassFile classFile, LocalVariables localVariables)
    {
        FastSwitch.Pair[] pairs = fs.getPairs();
        if (pairs == null) {
            return null;
        }

        for (FastSwitch.Pair pair : pairs) {
            if (pair == null || pair.getInstructions() == null) {
                continue;
            }
            for (Instruction instruction : pair.getInstructions()) {
                if (instruction != null) {
                    String signature = instruction.getReturnedSignature(classFile, localVariables);
                    if (signature != null) {
                        return signature;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public int getLastOffset()
    {
        int lastOffset = getOffset();
        FastSwitch.Pair[] pairs = fs.getPairs();
        if (pairs == null) {
            return lastOffset;
        }

        for (FastSwitch.Pair pair : pairs) {
            if (pair == null || pair.getInstructions() == null) {
                continue;
            }
            for (Instruction instruction : pair.getInstructions()) {
                if (instruction != null) {
                    lastOffset = Math.max(lastOffset, instruction.getLastOffset());
                }
            }
        }

        return lastOffset;
    }

    public FastSwitch getSwitch() {
        return fs;
    }
}
