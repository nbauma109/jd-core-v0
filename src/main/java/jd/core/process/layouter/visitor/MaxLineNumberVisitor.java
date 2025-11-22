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

import java.util.List;

import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.AssertInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.ConvertInstruction;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.MultiANewArray;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.Switch;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.attribute.ObjectrefAttribute;
import jd.core.model.instruction.bytecode.instruction.attribute.ValuerefAttribute;
import jd.core.model.instruction.fast.FastConstants;
import jd.core.model.instruction.fast.instruction.FastDeclaration;

public final class MaxLineNumberVisitor
{
    private MaxLineNumberVisitor() {
        super();
    }

    public static int visit(Instruction instruction)
    {
        int maxLineNumber = instruction.getLineNumber();

        switch (instruction.getOpcode())
        {
        case ByteCodeConstants.ARRAYLOAD:
            maxLineNumber = visit(((ArrayLoadInstruction)instruction).getIndexref());
            break;
        case ByteCodeConstants.ARRAYSTORE,
             ByteCodeConstants.STORE,
             ByteCodeConstants.XRETURN,
             Const.AASTORE,
             Const.ASTORE,
             Const.ISTORE,
             Const.PUTFIELD,
             Const.PUTSTATIC:
            maxLineNumber = visit(((ValuerefAttribute)instruction).getValueref());
            break;
        case ByteCodeConstants.ASSERT:
            {
                AssertInstruction ai = (AssertInstruction)instruction;
                maxLineNumber = visit(ai.getMsg() == null ? ai.getTest() : ai.getMsg());
            }
            break;
        case Const.ATHROW:
            maxLineNumber = visit(((AThrow)instruction).getValue());
            break;
        case ByteCodeConstants.UNARYOP:
            maxLineNumber = visit(((UnaryOperatorInstruction)instruction).getValue());
            break;
        case ByteCodeConstants.BINARYOP,
             ByteCodeConstants.ASSIGNMENT:
                BinaryOperatorInstruction boi = (BinaryOperatorInstruction)instruction;
                maxLineNumber = Math.max(visit(boi.getValue1()), visit(boi.getValue2()));
            break;
        case ByteCodeConstants.DUPSTORE,
             ByteCodeConstants.TERNARYOPSTORE,
             Const.CHECKCAST,
             Const.INSTANCEOF,
             Const.MONITORENTER,
             Const.MONITOREXIT,
             Const.POP,
             Const.GETFIELD:
            maxLineNumber = visit(((ObjectrefAttribute)instruction).getObjectref());
            break;
        case ByteCodeConstants.CONVERT,
             ByteCodeConstants.IMPLICITCONVERT:
            maxLineNumber = visit(((ConvertInstruction)instruction).getValue());
            break;
        case FastConstants.DECLARE:
            {
                FastDeclaration fd = (FastDeclaration)instruction;
                if (fd.getInstruction() != null) {
                    maxLineNumber = visit(fd.getInstruction());
                }
            }
            break;
        case ByteCodeConstants.IFCMP:
            IfCmp ifCmp = (IfCmp)instruction;
            maxLineNumber = Math.max(visit(ifCmp.getValue1()), visit(ifCmp.getValue2()));
            break;
        case ByteCodeConstants.IF,
             ByteCodeConstants.IFXNULL:
            maxLineNumber = visit(((IfInstruction)instruction).getValue());
            break;
        case ByteCodeConstants.COMPLEXIF:
            {
                List<Instruction> branchList =
                    ((ComplexConditionalBranchInstruction)instruction).getInstructions();
                maxLineNumber = visit(branchList.get(branchList.size()-1));
            }
            break;
        case Const.INVOKEINTERFACE,
             Const.INVOKESPECIAL,
             Const.INVOKEVIRTUAL,
             Const.INVOKESTATIC:
            {
                List<Instruction> list = ((InvokeInstruction)instruction).getArgs();
                int length = list.size();

                if (length == 0)
                {
                    if (instruction instanceof InvokeNoStaticInstruction insi) {
                        maxLineNumber = visit(insi.getObjectref());
                    } else {
                        maxLineNumber = instruction.getLineNumber();
                    }
                }
                else
                {
                    // Correction pour un tres curieux bug : les numéros de
                    // ligne des parameters ne sont pas toujours en ordre croissant
                    maxLineNumber = computeMaxLineNumber(list);
                }
            }
            break;
        case ByteCodeConstants.INVOKENEW,
             FastConstants.ENUMVALUE:
            {
                List<Instruction> list = ((InvokeNew)instruction).getArgs();
                if (list.isEmpty())
                {
                    maxLineNumber = instruction.getLineNumber();
                }
                else
                {
                    maxLineNumber = computeMaxLineNumber(list);
                }
            }
            break;
        case Const.LOOKUPSWITCH, Const.TABLESWITCH:
            maxLineNumber = visit(((Switch)instruction).getKey());
            break;
        case Const.MULTIANEWARRAY:
            {
                List<Instruction> dimensions = ((MultiANewArray)instruction).getDimensions();
                if (!dimensions.isEmpty()) {
                    maxLineNumber = visit(dimensions.get(dimensions.size()-1));
                }
            }
            break;
        case Const.NEWARRAY:
            maxLineNumber = visit(((NewArray)instruction).getDimension());
            break;
        case Const.ANEWARRAY:
            maxLineNumber = visit(((ANewArray)instruction).getDimension());
            break;
        case ByteCodeConstants.PREINC,
             ByteCodeConstants.POSTINC:
            IncInstruction ii = (IncInstruction)instruction;
            maxLineNumber = visit(ii.getValue());
            break;
        case ByteCodeConstants.INITARRAY,
             ByteCodeConstants.NEWANDINITARRAY:
            {
                InitArrayInstruction iai = (InitArrayInstruction)instruction;
                int length = iai.getValues().size();
                if (length > 0) {
                    maxLineNumber = visit(iai.getValues().get(length-1));
                }
            }
            break;
        case ByteCodeConstants.TERNARYOP:
            maxLineNumber = visit(((TernaryOperator)instruction).getValue2());
            break;
//        TODO check whether this necessary
//        case FastConstants.SYNCHRONIZED:
//            List<Instruction> instructions = ((FastSynchronized)instruction).getInstructions();
//            if (instructions != null && !instructions.isEmpty()) {
//                maxLineNumber = visit(instructions.get(instructions.size() - 1));
//            }
//            break;
        }

        // Autre curieux bug : les constantes finales passees en parameters
        // peuvent avoir un numéro de ligne plus petit que le numéro de ligne
        // de l'instruction INVOKE*
        if (maxLineNumber < instruction.getLineNumber())
        {
            return instruction.getLineNumber();
        }
        return maxLineNumber;
    }

    private static int computeMaxLineNumber(List<Instruction> instructions) {
        int maxLineNumber = visit(instructions.get(0));

        for (Instruction instruction : instructions)
        {
            int lineNumber = visit(instruction);
            if (maxLineNumber < lineNumber) {
                maxLineNumber = lineNumber;
            }
        }
        return maxLineNumber;
    }
}
