/*******************************************************************************
 * Copyright (C) 2022 GPLv3
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
package jd.core.process.layouter.visitor;

import java.util.List;

import jd.core.model.instruction.bytecode.instruction.Instruction;

public final class MinMaxLineNumberVisitor {

    private MinMaxLineNumberVisitor() {
    }

    public static MinMaxLineNumber visit(List<Instruction> instructions) {
        int minLineNumber = Integer.MAX_VALUE;
        int maxLineNumber = Integer.MIN_VALUE;
        for (Instruction instruction : instructions) {
            minLineNumber = Math.min(minLineNumber, MinLineNumberVisitor.visit(instruction));
            maxLineNumber = Math.max(maxLineNumber, MaxLineNumberVisitor.visit(instruction));
        }
        return new MinMaxLineNumber(minLineNumber, maxLineNumber);
    }

    public record MinMaxLineNumber(int minLineNumber, int maxLineNumber) {

        public boolean isValid() {
            return minLineNumber != Integer.MAX_VALUE && maxLineNumber != Integer.MIN_VALUE;
        }
    }
}
