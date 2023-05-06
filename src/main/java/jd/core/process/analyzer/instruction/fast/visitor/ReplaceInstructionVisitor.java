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
package jd.core.process.analyzer.instruction.fast.visitor;

import org.apache.bcel.Const;

import java.util.List;

import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ArrayLength;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
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
import jd.core.model.instruction.bytecode.instruction.MultiANewArray;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.Switch;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.attribute.ObjectrefAttribute;
import jd.core.model.instruction.bytecode.instruction.attribute.ValuerefAttribute;
import jd.core.model.instruction.fast.FastConstants;
import jd.core.model.instruction.fast.instruction.FastDeclaration;
import jd.core.model.instruction.fast.instruction.FastSynchronized;
import jd.core.model.instruction.fast.instruction.FastTestList;
import jd.core.model.instruction.fast.instruction.FastTry;

/*
 * utilisé par TernaryOpReconstructor
 */
public class ReplaceInstructionVisitor
{
    private int offset;
    private Instruction newInstruction;
    private Instruction oldInstruction;

    public ReplaceInstructionVisitor(int offset, Instruction newInstruction)
    {
        init(offset, newInstruction);
    }

    public void init(int offset, Instruction newInstruction)
    {
        this.offset = offset;
        this.newInstruction = newInstruction;
        this.oldInstruction = null;
    }

    public void visit(Instruction instruction)
    {
        switch (instruction.getOpcode())
        {
        case Const.ARRAYLENGTH:
            {
                ArrayLength al = (ArrayLength)instruction;
                if (al.getArrayref().getOffset() == this.offset)
                {
                    this.oldInstruction = al.getArrayref();
                    al.setArrayref(this.newInstruction);
                }
                else
                {
                    visit(al.getArrayref());
                }
            }
            break;
        case Const.AASTORE,
             ByteCodeConstants.ARRAYSTORE:
            {
                ArrayStoreInstruction asi = (ArrayStoreInstruction)instruction;
                if (asi.getArrayref().getOffset() == this.offset)
                {
                    this.oldInstruction = asi.getArrayref();
                    asi.setArrayref(this.newInstruction);
                }
                else
                {
                    visit(asi.getArrayref());

                    if (this.oldInstruction == null)
                    {
                        if (asi.getIndexref().getOffset() == this.offset)
                        {
                            this.oldInstruction = asi.getIndexref();
                            asi.setIndexref(this.newInstruction);
                        }
                        else
                        {
                            visit(asi.getIndexref());

                            if (this.oldInstruction == null)
                            {
                                if (asi.getValueref().getOffset() == this.offset)
                                {
                                    this.oldInstruction = asi.getValueref();
                                    asi.setValueref(this.newInstruction);
                                }
                                else
                                {
                                    visit(asi.getValueref());
                                }
                            }
                        }
                    }
                }
            }
            break;
        case ByteCodeConstants.ASSERT:
            {
                AssertInstruction ai = (AssertInstruction)instruction;
                if (ai.getTest().getOffset() == this.offset)
                {
                    this.oldInstruction = ai.getTest();
                    ai.setTest(this.newInstruction);
                }
                else
                {
                    visit(ai.getTest());

                    if (this.oldInstruction == null && ai.getMsg() != null)
                    {
                        if (ai.getMsg().getOffset() == this.offset)
                        {
                            this.oldInstruction = ai.getMsg();
                            ai.setMsg(this.newInstruction);
                        }
                        else
                        {
                            visit(ai.getMsg());
                        }
                    }
                }
            }
            break;
        case Const.ATHROW:
            {
                AThrow aThrow = (AThrow)instruction;
                if (aThrow.getValue().getOffset() == this.offset)
                {
                    this.oldInstruction = aThrow.getValue();
                    aThrow.setValue(this.newInstruction);
                }
                else
                {
                    visit(aThrow.getValue());
                }
            }
            break;
        case ByteCodeConstants.UNARYOP:
            {
                UnaryOperatorInstruction uoi = (UnaryOperatorInstruction)instruction;
                if (uoi.getValue().getOffset() == this.offset)
                {
                    this.oldInstruction = uoi.getValue();
                    uoi.setValue(this.newInstruction);
                }
                else
                {
                    visit(uoi.getValue());
                }
            }
            break;
        case ByteCodeConstants.BINARYOP,
             ByteCodeConstants.ASSIGNMENT:
            {
                BinaryOperatorInstruction boi = (BinaryOperatorInstruction)instruction;
                if (boi.getValue1().getOffset() == this.offset)
                {
                    this.oldInstruction = boi.getValue1();
                    boi.setValue1(this.newInstruction);
                }
                else
                {
                    visit(boi.getValue1());

                    if (this.oldInstruction == null)
                    {
                        if (boi.getValue2().getOffset() == this.offset)
                        {
                            this.oldInstruction = boi.getValue2();
                            boi.setValue2(this.newInstruction);
                        }
                        else
                        {
                            visit(boi.getValue2());
                        }
                    }
                }
            }
            break;
        case ByteCodeConstants.DUPSTORE,
             ByteCodeConstants.TERNARYOPSTORE,
             Const.CHECKCAST,
             Const.INSTANCEOF,
             Const.MONITORENTER,
             Const.MONITOREXIT,
             Const.POP,
             Const.GETFIELD:
            {
                visitObjectref(instruction);
            }
            break;
        case ByteCodeConstants.XRETURN,
             ByteCodeConstants.STORE,
             Const.ASTORE,
             Const.ISTORE,
             Const.PUTSTATIC:
            {
                ValuerefAttribute v = (ValuerefAttribute)instruction;
                if (v.getValueref().getOffset() == this.offset)
                {
                    this.oldInstruction = v.getValueref();
                    v.setValueref(this.newInstruction);
                }
                else
                {
                    visit(v.getValueref());
                }
            }
            break;
        case ByteCodeConstants.CONVERT,
             ByteCodeConstants.IMPLICITCONVERT:
            {
                ConvertInstruction ci = (ConvertInstruction)instruction;
                if (ci.getValue().getOffset() == this.offset)
                {
                    this.oldInstruction = ci.getValue();
                    ci.setValue(this.newInstruction);
                }
                else
                {
                    visit(ci.getValue());
                }
            }
            break;
        case ByteCodeConstants.IFCMP:
            {
                IfCmp ifCmp = (IfCmp)instruction;
                if (ifCmp.getValue1().getOffset() == this.offset)
                {
                    this.oldInstruction = ifCmp.getValue1();
                    ifCmp.setValue1(this.newInstruction);
                }
                else
                {
                    visit(ifCmp.getValue1());

                    if (this.oldInstruction == null)
                    {
                        if (ifCmp.getValue2().getOffset() == this.offset)
                        {
                            this.oldInstruction = ifCmp.getValue2();
                            ifCmp.setValue2(this.newInstruction);
                        }
                        else
                        {
                            visit(ifCmp.getValue2());
                        }
                    }
                }
            }
            break;
        case ByteCodeConstants.IF,
             ByteCodeConstants.IFXNULL:
            {
                IfInstruction iff = (IfInstruction)instruction;
                if (iff.getValue().getOffset() == this.offset)
                {
                    this.oldInstruction = iff.getValue();
                    iff.setValue(this.newInstruction);
                }
                else
                {
                    visit(iff.getValue());
                }
            }
            break;
        case ByteCodeConstants.COMPLEXIF:
            {
                visit(((ComplexConditionalBranchInstruction)instruction).getInstructions());
            }
            break;
        case Const.INVOKEINTERFACE,
             Const.INVOKESPECIAL,
             Const.INVOKEVIRTUAL:
            {
                visitObjectref(instruction);
            }
            // intended fall through
        case Const.INVOKESTATIC:
            {
                List<Instruction> list = ((InvokeInstruction)instruction).getArgs();
                for (int i=list.size()-1; i>=0 && this.oldInstruction == null; --i)
                {
                    Instruction instuction = list.get(i);
                    if (instuction.getOffset() == this.offset)
                    {
                        this.oldInstruction = instuction;
                        list.set(i, this.newInstruction);
                    }
                    else
                    {
                        visit(instuction);
                    }
                }
            }
            break;
        case ByteCodeConstants.INVOKENEW:
            {
                List<Instruction> list = ((InvokeNew)instruction).getArgs();
                for (int i=list.size()-1; i>=0 && this.oldInstruction == null; --i)
                {
                    Instruction instuction = list.get(i);
                    if (instuction.getOffset() == this.offset)
                    {
                        this.oldInstruction = instuction;
                        list.set(i, this.newInstruction);
                    }
                    else
                    {
                        visit(instuction);
                    }
                }
            }
            break;
        case Const.LOOKUPSWITCH, Const.TABLESWITCH:
            {
                Switch ls = (Switch)instruction;
                if (ls.getKey().getOffset() == this.offset)
                {
                    this.oldInstruction = ls.getKey();
                    ls.setKey(this.newInstruction);
                }
                else
                {
                    visit(ls.getKey());
                }
            }
            break;
        case Const.MULTIANEWARRAY:
            {
                List<Instruction> dimensions = ((MultiANewArray)instruction).getDimensions();
                for (int i=dimensions.size()-1; i>=0 && this.oldInstruction == null; --i)
                {
                    if (dimensions.get(i).getOffset() == this.offset)
                    {
                        this.oldInstruction = dimensions.get(i);
                        dimensions.set(i, this.newInstruction);
                    }
                    else
                    {
                        visit(dimensions.get(i));
                    }
                }
            }
            break;
        case Const.NEWARRAY:
            {
                NewArray newArray = (NewArray)instruction;
                if (newArray.getDimension().getOffset() == this.offset)
                {
                    this.oldInstruction = newArray.getDimension();
                    newArray.setDimension(this.newInstruction);
                }
                else
                {
                    visit(newArray.getDimension());
                }
            }
            break;
        case Const.ANEWARRAY:
            {
                ANewArray aNewArray = (ANewArray)instruction;
                if (aNewArray.getDimension().getOffset() == this.offset)
                {
                    this.oldInstruction = aNewArray.getDimension();
                    aNewArray.setDimension(this.newInstruction);
                }
                else
                {
                    visit(aNewArray.getDimension());
                }
            }
            break;
        case Const.PUTFIELD:
            {
                PutField putField = (PutField)instruction;
                if (putField.getObjectref().getOffset() == this.offset)
                {
                    this.oldInstruction = putField.getObjectref();
                    putField.setObjectref(this.newInstruction);
                }
                else
                {
                    visit(putField.getObjectref());

                    if (this.oldInstruction == null)
                    {
                        int targetOffset = this.offset;
                        if (putField.getValueref().getOpcode() == ByteCodeConstants.PREINC) {
                            targetOffset--;
                        }
                        if (putField.getValueref().getOffset() == targetOffset)
                        {
                            this.oldInstruction = putField.getValueref();
                            putField.setValueref(this.newInstruction);
                        }
                        else
                        {
                            visit(putField.getValueref());
                        }
                    }
                }
            }
            break;
        case ByteCodeConstants.PREINC,
             ByteCodeConstants.POSTINC:
            {
                IncInstruction ii = (IncInstruction)instruction;
                if (ii.getValue().getOffset() == this.offset)
                {
                    this.oldInstruction = ii.getValue();
                    ii.setValue(this.newInstruction);
                }
                else
                {
                    visit(ii.getValue());
                }
            }
            break;
        case ByteCodeConstants.INITARRAY,
             ByteCodeConstants.NEWANDINITARRAY:
            {
                InitArrayInstruction iai = (InitArrayInstruction)instruction;
                if (iai.getNewArray().getOffset() == this.offset)
                {
                    this.oldInstruction = iai.getNewArray();
                    iai.setNewArray(this.newInstruction);
                }
                else
                {
                    visit(iai.getNewArray());

                    if (iai.getValues() != null) {
                        visit(iai.getValues());
                    }
                }
            }
            break;
        case ByteCodeConstants.TERNARYOP:
            {
                TernaryOperator to = (TernaryOperator)instruction;
                if (to.getTest().getOffset() == this.offset)
                {
                    this.oldInstruction = to.getTest();
                    to.setTest(this.newInstruction);
                }
                else
                {
                    visit(to.getTest());

                    if (this.oldInstruction == null)
                    {
                        if (to.getValue1().getOffset() == this.offset)
                        {
                            this.oldInstruction = to.getValue1();
                            to.setValue1(this.newInstruction);
                        }
                        else
                        {
                            visit(to.getValue1());

                            if (this.oldInstruction == null)
                            {
                                if (to.getValue2().getOffset() == this.offset)
                                {
                                    this.oldInstruction = to.getValue2();
                                    to.setValue2(this.newInstruction);
                                }
                                else
                                {
                                    visit(to.getValue2());
                                }
                            }
                        }
                    }
                }
            }
            break;
        case FastConstants.TRY:
            {
                FastTry ft = (FastTry)instruction;
                visit(ft.getResources());
                visit(ft.getInstructions());

                if (this.oldInstruction == null)
                {
                    if (ft.getFinallyInstructions() != null) {
                        visit(ft.getFinallyInstructions());
                    }

                    for (int i=ft.getCatches().size()-1; i>=0 && this.oldInstruction == null; --i) {
                        visit(ft.getCatches().get(i).instructions());
                    }
                }
            }
            break;
        case FastConstants.DECLARE:
            {
                FastDeclaration fd = (FastDeclaration)instruction;

                if (fd.getInstruction() != null)
                {
                    if (fd.getInstruction().getOffset() == this.offset)
                    {
                        this.oldInstruction = fd.getInstruction();
                        fd.setInstruction(this.newInstruction);
                    }
                    else
                    {
                        visit(fd.getInstruction());
                    }
                }
            }
            break;
        case FastConstants.SYNCHRONIZED:
            {
                FastSynchronized fsy = (FastSynchronized)instruction;

                if (fsy.getMonitor().getOffset() == this.offset)
                {
                    this.oldInstruction = fsy.getMonitor();
                    fsy.setMonitor(this.newInstruction);
                }
                else
                {
                    visit(fsy.getMonitor());

                    if (this.oldInstruction == null) {
                        visit(fsy.getInstructions());
                    }
                }
            }
            break;
        case FastConstants.IF_SIMPLE:
            {
                FastTestList ftl = (FastTestList)instruction;

                if (ftl.getTest().getOffset() == this.offset)
                {
                    this.oldInstruction = ftl.getTest();
                    ftl.setTest(this.newInstruction);
                }
                else
                {
                    visit(ftl.getTest());

                    if (this.oldInstruction == null &&
                        ftl.getInstructions() != null) {
                        visit(ftl.getInstructions());
                    }
                }
            }
            break;
        case Const.ACONST_NULL,
             ByteCodeConstants.ARRAYLOAD,
             ByteCodeConstants.LOAD,
             Const.ALOAD,
             Const.ILOAD,
             Const.BIPUSH,
             ByteCodeConstants.ICONST,
             ByteCodeConstants.LCONST,
             ByteCodeConstants.FCONST,
             ByteCodeConstants.DCONST,
             ByteCodeConstants.DUPLOAD,
             Const.GETSTATIC,
             ByteCodeConstants.OUTERTHIS,
             Const.GOTO,
             Const.IINC,
             Const.JSR,
             Const.LDC,
             Const.LDC2_W,
             Const.NEW,
             Const.NOP,
             Const.SIPUSH,
             Const.RET,
             Const.RETURN,
             Const.INVOKEDYNAMIC,
             ByteCodeConstants.EXCEPTIONLOAD,
             ByteCodeConstants.RETURNADDRESSLOAD:
            break;
        default:
            System.err.println(
                    "Can not replace code in " +
                    instruction.getClass().getName() +
                    ", opcode=" + instruction.getOpcode());
        }
    }

    private void visitObjectref(Instruction instruction) {
        ObjectrefAttribute o = (ObjectrefAttribute)instruction;
        if (o.getObjectref().getOffset() == this.offset)
        {
            this.oldInstruction = o.getObjectref();
            o.setObjectref(this.newInstruction);
        }
        else
        {
            visit(o.getObjectref());
        }
    }

    private void visit(List<Instruction> instructions)
    {
        for (int i=instructions.size()-1; i>=0; --i) {
            visit(instructions.get(i));
        }
    }

    public Instruction getOldInstruction()
    {
        return oldInstruction;
    }

    public void setOldInstruction(Instruction oldInstruction)
    {
        this.oldInstruction = oldInstruction;
    }
}
