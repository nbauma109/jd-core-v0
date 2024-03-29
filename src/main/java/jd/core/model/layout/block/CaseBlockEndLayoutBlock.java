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

import jd.core.model.instruction.bytecode.instruction.Instruction;

public class CaseBlockEndLayoutBlock extends LayoutBlock
{
    private final boolean bracketNeeded;

    public CaseBlockEndLayoutBlock(boolean bracketNeeded)
    {
        super(
            LayoutBlockConstants.CASE_BLOCK_END,
            Instruction.UNKNOWN_LINE_NUMBER,
            Instruction.UNKNOWN_LINE_NUMBER,
            0, LayoutBlockConstants.UNLIMITED_LINE_COUNT, 1);
        this.bracketNeeded = bracketNeeded;
    }

    public boolean isBracketNeeded() {
        return bracketNeeded;
    }
}
