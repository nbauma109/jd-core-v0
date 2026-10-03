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
package jd.core.process.layouter.visitor;

import org.apache.bcel.Const;

import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.AssignmentInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.model.instruction.bytecode.instruction.attribute.ObjectrefAttribute;
import jd.core.model.instruction.fast.FastConstants;
import jd.core.model.instruction.fast.instruction.FastSwitch;
import jd.core.model.instruction.fast.instruction.FastDeclaration;

public final class MinLineNumberVisitor
{
    private MinLineNumberVisitor() {
    }

    public static int visit(Instruction instruction)
    {
        switch (instruction.getOpcode())
        {
        case FastConstants.DECLARE:
            FastDeclaration declaration = (FastDeclaration) instruction;
            return declaration.getInstruction() == null ? instruction.getLineNumber()
                    : earlierKnownLine(instruction, visit(declaration.getInstruction()));
        case Const.PUTSTATIC:
            return earlierKnownLine(instruction, visit(((PutStatic) instruction).getValueref()));
        case ByteCodeConstants.STORE,
             Const.ASTORE,
             Const.ISTORE:
            return earlierKnownLine(instruction, visit(((StoreInstruction) instruction).getValueref()));
        case ByteCodeConstants.ARRAYLOAD,
             ByteCodeConstants.ARRAYSTORE,
             Const.AASTORE:
            return knownLineOrChild(instruction, visit(((ArrayInstruction)instruction).getArrayref()));
        case ByteCodeConstants.ASSIGNMENT:
            return knownLineOrChild(instruction, visit(((AssignmentInstruction)instruction).getValue1()));
        case ByteCodeConstants.BINARYOP:
            return knownLineOrChild(instruction, visit(((BinaryOperatorInstruction)instruction).getValue1()));
        case ByteCodeConstants.PREINC:
            {
                IncInstruction ii = (IncInstruction)instruction;

                if (ii.isSingleStep())
                {
                    // Operator '++' or '--'
                    return instruction.getLineNumber();
                }
                return knownLineOrChild(instruction, visit(ii.getValue()));
            }
        case ByteCodeConstants.POSTINC:
            {
                IncInstruction ii = (IncInstruction)instruction;

                if (ii.isSingleStep())
                {
                    // Operator '++' or '--'
                    return knownLineOrChild(instruction, visit(ii.getValue()));
                }
                return instruction.getLineNumber();
            }
        case Const.CHECKCAST,
             Const.INSTANCEOF,
             Const.INVOKEINTERFACE,
             Const.INVOKEVIRTUAL,
             Const.INVOKESPECIAL,
             Const.POP,
             Const.PUTFIELD:
            return knownLineOrChild(instruction, visit(((ObjectrefAttribute)instruction).getObjectref()));
        case ByteCodeConstants.TERNARYOP:
            return knownLineOrChild(instruction, visit(((TernaryOperator)instruction).getTest()));
        case FastConstants.SWITCH,
             FastConstants.SWITCH_ENUM,
             FastConstants.SWITCH_STRING:
            {
                FastSwitch fs = (FastSwitch)instruction;
                int min = visit(fs.getTest());
                for (FastSwitch.Pair pair : fs.getPairs())
                {
                    if (pair.getInstructions() == null) {
                        continue;
                    }
                    for (Instruction i : pair.getInstructions())
                    {
                        int candidate = visit(i);
                        if (min == Instruction.UNKNOWN_LINE_NUMBER || (candidate != Instruction.UNKNOWN_LINE_NUMBER && candidate < min)) {
                            min = candidate;
                        }
                    }
                }
                return knownLineOrChild(instruction, min);
            }
        }

        return instruction.getLineNumber();
    }

    private static int earlierKnownLine(Instruction instruction, int childLineNumber) {
        int lineNumber = instruction.getLineNumber();
        if (lineNumber == Instruction.UNKNOWN_LINE_NUMBER) {
            return childLineNumber;
        }
        return childLineNumber == Instruction.UNKNOWN_LINE_NUMBER ? lineNumber : Math.min(lineNumber, childLineNumber);
    }

    private static int knownLineOrChild(Instruction instruction, int childLineNumber) {
        // Synthetic receivers and targets can lack a line table entry even
        // when the enclosing expression has a valid source line.
        return childLineNumber == Instruction.UNKNOWN_LINE_NUMBER
                ? instruction.getLineNumber() : childLineNumber;
    }
}
