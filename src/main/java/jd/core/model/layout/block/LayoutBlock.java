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

import jd.core.model.layout.section.LayoutSection;

/*
 * bloc(i).lastLineNumber = bloc(i).firstLineNumber + bloc(i).minimalLineCount
 *
 * bloc(i).firstLineNumber + bloc(i).minimalLineCount <=
 *   bloc(i+1).firstLineNumber <=
 *     bloc(i).firstLineNumber + bloc(i).maximalLineCount
 */
public class LayoutBlock {

    private byte tag;

    private int firstLineNumber;
    private int lastLineNumber;

    private int minimalLineCount;
    private int maximalLineCount;
    private int preferedLineCount;

    private int lineCount;
    private boolean lineNumbersDiscarded;
    private boolean instructionLineSpanLimited;

    private int index;
    private LayoutSection section;

    public LayoutBlock(
        byte tag, int firstLineNumber, int lastLineNumber, int lineCount)
    {
        this.setTag(tag);
        this.firstLineNumber = firstLineNumber;
        this.lastLineNumber = lastLineNumber;
        this.minimalLineCount = lineCount;
        this.maximalLineCount = lineCount;
        this.setPreferedLineCount(lineCount);
        this.setLineCount(lineCount);
        this.setIndex(0);
        this.setSection(null);
    }

    public LayoutBlock(
        byte tag, int firstLineNumber, int lastLineNumber,
        int minimalLineCount, int maximalLineCount, int preferedLineCount)
    {
        this.setTag(tag);
        this.firstLineNumber = firstLineNumber;
        this.lastLineNumber = lastLineNumber;
        this.minimalLineCount = minimalLineCount;
        this.maximalLineCount = maximalLineCount;
        this.setPreferedLineCount(preferedLineCount);
        this.setLineCount(preferedLineCount);
        this.setIndex(0);
        this.setSection(null);
    }

    public int getLastLineNumber() {
        return lastLineNumber;
    }

    public int getMaximalLineCount() {
        return maximalLineCount;
    }

    public int getMinimalLineCount() {
        return minimalLineCount;
    }

    public int getFirstLineNumber() {
        return firstLineNumber;
    }

    public void setFirstLineNumber(int firstLineNumber) {
        this.firstLineNumber = firstLineNumber;
    }

    public void setLastLineNumber(int lastLineNumber) {
        this.lastLineNumber = lastLineNumber;
    }

    /** The line numbers of this block do not belong to its place: they must not drive the layout. */
    public boolean isLineNumbersDiscarded() {
        return lineNumbersDiscarded;
    }

    /**
     * Forget the line numbers: the block is then printed on as many lines as
     * it needs, whatever the numbers of its instructions.
     */
    public void discardLineNumbers() {
        this.firstLineNumber = 0;
        this.lastLineNumber = 0;
        this.lineNumbersDiscarded = true;
        this.minimalLineCount = 0;
        this.maximalLineCount = 0;
        this.setPreferedLineCount(0);
        this.setLineCount(0);
    }

    /** Set the fixed source span used by the instruction or bytecode writer. */
    public void setInstructionLineSpan(int lastLineNumber) {
        this.lastLineNumber = lastLineNumber;
        this.instructionLineSpanLimited = true;
        int span = lastLineNumber - firstLineNumber;
        this.minimalLineCount = span;
        this.maximalLineCount = span;
        this.setPreferedLineCount(span);
        this.setLineCount(span);
    }

    public int getInstructionLineNumberLimit() {
        return instructionLineSpanLimited ? lastLineNumber : 0;
    }

    public byte getTag() {
        return tag;
    }

    protected void setTag(byte tag) {
        this.tag = tag;
    }

    public int getLineCount() {
        return lineCount;
    }

    public int setLineCount(int lineCount) {
        this.lineCount = lineCount;
        return lineCount;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public LayoutSection getSection() {
        return section;
    }

    public void setSection(LayoutSection section) {
        this.section = section;
    }

    public int getPreferedLineCount() {
        return preferedLineCount;
    }

    public void setPreferedLineCount(int preferedLineCount) {
        this.preferedLineCount = preferedLineCount;
    }
}
