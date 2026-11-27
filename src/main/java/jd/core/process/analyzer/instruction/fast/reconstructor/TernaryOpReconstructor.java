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
package jd.core.process.analyzer.instruction.fast.reconstructor;

import java.util.List;

import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.BranchInstruction;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.TernaryOpStore;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.process.analyzer.instruction.bytecode.ComparisonInstructionAnalyzer;
import jd.core.process.analyzer.instruction.bytecode.util.ByteCodeUtil;
import jd.core.process.analyzer.instruction.fast.visitor.ReplaceInstructionVisitor;

/*
 * Reconstruction des operateurs ternaires.
 * Contenu de la liste d'instructions :
 *  offset-1) If instruction (IF || IFCMP || IFXNULL || COMPLEXIF)
 *    offset) TernaryOpStore
 *  offset+1) Goto
 */
public final class TernaryOpReconstructor
{
    private TernaryOpReconstructor() {
    }

    public static void reconstruct(List<Instruction> list)
    {
        int length = list.size();

        for (int index=1; index<length; ++index)
        {
            Instruction i = list.get(index);

            if (i.getOpcode() == ByteCodeConstants.TERNARYOPSTORE &&
                index+2<length)
            {
                // Search test
                Instruction gi = list.get(index+1);
                Instruction afterGi = list.get(index+2);
                Instruction test = null;
                int indexTest = index;

                while (indexTest-- > 0)
                {
                    Instruction instruction = list.get(indexTest);
                    int opcode = instruction.getOpcode();

                    if (ByteCodeUtil.isIfInstruction(opcode, true))
                    {
                        int jumpOffset =
                            ((BranchInstruction)instruction).getJumpOffset();
                        if (gi.getOffset() < jumpOffset &&
                            jumpOffset <= afterGi.getOffset())
                        {
                            test = instruction;
                            break;
                        }
                    }
                }

                if (test == null) {
                    continue;
                }

                TernaryOpStore value1 = (TernaryOpStore)i;

                ComparisonInstructionAnalyzer.inverseComparison(test);

                TernaryOperator fto = new TernaryOperator(
                    ByteCodeConstants.TERNARYOP,
                    value1.getTernaryOp2ndValueOffset(), test.getLineNumber(),
                    test, value1.getObjectref(), null);

                ReplaceInstructionVisitor visitor =
                    new ReplaceInstructionVisitor(
                            value1.getTernaryOp2ndValueOffset(), fto);

                int indexVisitor;
                for (indexVisitor = index+2; indexVisitor<length && visitor.getOldInstruction()==null; indexVisitor++) {
                   Instruction instruction = list.get(indexVisitor);
                   if (instruction.getOffset() == value1.getTernaryOp2ndValueOffset()) {
                       visitor.setOldInstruction(instruction);
                       list.set(indexVisitor, fto);
                   } else {
                       visitor.visit(instruction);
                   }
                }

                if (visitor.getOldInstruction() instanceof TernaryOperator op) {
                    if (isInFalseBranch(test, op)) {
                        nestInFalseBranch(fto, op);
                    } else {
                        encloseInTrueBranch(fto, op);
                    }
                } else {
                    fto.setValue2(visitor.getOldInstruction());
                }

                if (isBooleanConstant(fto.getValue2()) && fto.getValue1() instanceof IConst iConst1)
                {
                    iConst1.setSignature("Z");
                }

                if (isBooleanConstant(fto.getValue1())
                        && isOtherConstant(fto.getValue2(), (IConst)fto.getValue1())
                        && !isNestedLater(list, index+2, value1.getTernaryOp2ndValueOffset()))
                {
                    if (((IConst)fto.getValue1()).getValue() == 0) {
                        ComparisonInstructionAnalyzer.inverseComparison(fto.getTest());
                    }

                    visitor.init(fto.getOffset(), fto.getTest());

                    indexVisitor = index+2;
                    while (indexVisitor<length && visitor.getOldInstruction()==null) {
                        visitor.visit(list.get(indexVisitor++));
                    }
                }

                // Remove Goto
                list.remove(index+1);
                // Remove TernaryOpStore
                list.remove(index);
                // Remove test
                list.remove(indexTest);

                index -= 2;
                length -= 3;
            }
        }
    }

    /** @return true if the test follows the one of the operator, as in "a ? x : b ? y : z" */
    private static boolean isInFalseBranch(Instruction test, TernaryOperator operator)
    {
        return test.getOffset() > operator.getTest().getOffset();
    }

    /**
     * The condition of the new operator precedes the ones of the operator it replaces in the instructions, which is
     * then the value given when it is true, as in "a ? b ? x : y : z". The new value is the last one the latter gives
     * when its conditions are false.
     */
    private static void encloseInTrueBranch(TernaryOperator fto, TernaryOperator operator)
    {
        TernaryOperator last = operator;
        while (last.getValue2() instanceof TernaryOperator child) {
            last = child;
        }
        fto.setValue2(last.getValue2());
        last.setValue2(fto.getValue1());
        fto.setValue1(operator);
    }

    /**
     * Gives the new operator, which has replaced the outer one in the instructions, the content of the outer one, and
     * nests its own content in the last false branch testing a condition before its own.
     */
    private static void nestInFalseBranch(TernaryOperator fto, TernaryOperator outer)
    {
        TernaryOperator inner = new TernaryOperator(
            ByteCodeConstants.TERNARYOP, fto.getOffset(), fto.getLineNumber(),
            fto.getTest(), fto.getValue1(), null);

        fto.setTest(outer.getTest());
        fto.setValue1(outer.getValue1());
        fto.setValue2(outer.getValue2());
        fto.setLineNumber(outer.getLineNumber());

        TernaryOperator parent = fto;
        while (parent.getValue2() instanceof TernaryOperator child && isInFalseBranch(inner.getTest(), child)) {
            parent = child;
        }
        if (parent.getValue2() instanceof TernaryOperator child) {
            encloseInTrueBranch(inner, child);
        } else {
            inner.setValue2(parent.getValue2());
        }
        parent.setValue2(inner);
    }

    /** @return false if the values do not stand for "test ? true : false" or its opposite */
    private static boolean isOtherConstant(Instruction instruction, IConst constant)
    {
        return instruction instanceof IConst other && other.getValue() != constant.getValue();
    }

    /** @return true if another operator, with the same last value, is still to be nested in the new one */
    private static boolean isNestedLater(List<Instruction> list, int start, int ternaryOp2ndValueOffset)
    {
        return list.stream().skip(start).anyMatch(instruction -> instruction instanceof TernaryOpStore store
                && store.getTernaryOp2ndValueOffset() == ternaryOp2ndValueOffset);
    }

    private static boolean isBooleanConstant(Instruction instruction)
    {
        return instruction != null
            && ByteCodeUtil.isLoadIntValue(instruction.getOpcode())
            && "Z".equals(instruction.getReturnedSignature(null, null));
    }
}
