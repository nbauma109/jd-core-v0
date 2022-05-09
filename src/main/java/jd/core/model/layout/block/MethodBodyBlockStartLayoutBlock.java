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

public class MethodBodyBlockStartLayoutBlock extends BlockLayoutBlock
{
    public MethodBodyBlockStartLayoutBlock()
    {
        this(2);
    }

    public MethodBodyBlockStartLayoutBlock(int preferedLineCount)
    {
        super(
            LayoutBlockConstants.METHOD_BODY_BLOCK_START, 0,
            LayoutBlockConstants.UNLIMITED_LINE_COUNT, preferedLineCount);
    }

    public void transformToStartEndBlock(int preferedLineCount)
    {
        this.setTag(LayoutBlockConstants.METHOD_BODY_BLOCK_START_END);
        this.setPreferedLineCount(this.setLineCount(preferedLineCount));
    }

    public void transformToSingleLineBlock()
    {
        this.setTag(LayoutBlockConstants.METHOD_BODY_SINGLE_LINE_BLOCK_START);
    }
}