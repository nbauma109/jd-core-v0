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
package jd.core.process.analyzer.classfile.visitor;

import org.apache.bcel.Const;

import java.util.List;

import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ArrayLength;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.ConvertInstruction;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.IndexInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.MultiANewArray;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.Switch;
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.attribute.ObjectrefAttribute;
import jd.core.model.instruction.bytecode.instruction.attribute.ValuerefAttribute;

/*
 * Replace 'ALoad(1)' in constructor by 'OuterThis()':
 * replace '???.xxx' by 'TestInnerClass.this.xxx'.
 */
public class ReplaceOuterReferenceVisitor
{
    private final int opcode;
    private final int index;
    private final int outerThisInstructionIndex;

    public ReplaceOuterReferenceVisitor(
        int opcode, int index, int outerThisInstructionIndex)
    {
        this.opcode = opcode;
        this.index = index;
        this.outerThisInstructionIndex = outerThisInstructionIndex;
    }

    public void visit(Instruction instruction)
    {
        switch (instruction.getOpcode())
        {
        case Const.ARRAYLENGTH:
            {
                ArrayLength al = (ArrayLength)instruction;
                if (match(al.getArrayref())) {
                    al.setArrayref(newInstruction(al.getArrayref()));
                } else {
                    visit(al.getArrayref());
                }
            }
            break;
        case Const.AASTORE,
             ByteCodeConstants.ARRAYSTORE:
            {
                ArrayStoreInstruction asi = (ArrayStoreInstruction)instruction;
                if (match(asi.getArrayref())) {
                    asi.setArrayref(newInstruction(asi.getArrayref()));
                } else {
                    visit(asi.getArrayref());
                }
                if (match(asi.getIndexref())) {
                    asi.setIndexref(newInstruction(asi.getIndexref()));
                } else {
                    visit(asi.getIndexref());
                }
                if (match(asi.getValueref())) {
                    asi.setValueref(newInstruction(asi.getValueref()));
                } else {
                    visit(asi.getValueref());
                }
            }
            break;
        case Const.ATHROW:
            {
                AThrow aThrow = (AThrow)instruction;
                if (match(aThrow.getValue())) {
                    aThrow.setValue(newInstruction(aThrow.getValue()));
                } else {
                    visit(aThrow.getValue());
                }
            }
            break;
        case ByteCodeConstants.UNARYOP:
            {
                UnaryOperatorInstruction uoi = (UnaryOperatorInstruction)instruction;
                if (match(uoi.getValue())) {
                    uoi.setValue(newInstruction(uoi.getValue()));
                } else {
                    visit(uoi.getValue());
                }
            }
            break;
        case ByteCodeConstants.BINARYOP:
            {
                BinaryOperatorInstruction boi = (BinaryOperatorInstruction)instruction;
                if (match(boi.getValue1())) {
                    boi.setValue1(newInstruction(boi.getValue1()));
                } else {
                    visit(boi.getValue1());
                }
                if (match(boi.getValue2())) {
                    boi.setValue2(newInstruction(boi.getValue2()));
                } else {
                    visit(boi.getValue2());
                }
            }
            break;
        case ByteCodeConstants.DUPSTORE,
             ByteCodeConstants.TERNARYOPSTORE,
             Const.CHECKCAST,
             Const.GETFIELD,
             Const.INSTANCEOF,
             Const.MONITORENTER,
             Const.MONITOREXIT,
             Const.POP:
            {
                ObjectrefAttribute o = (ObjectrefAttribute)instruction;
                if (match(o.getObjectref())) {
                    o.setObjectref(newInstruction(o.getObjectref()));
                } else {
                    visit(o.getObjectref());
                }
            }
            break;
        case ByteCodeConstants.STORE,
             ByteCodeConstants.XRETURN,
             Const.ASTORE,
             Const.ISTORE,
             Const.PUTSTATIC:
            {
                ValuerefAttribute v = (ValuerefAttribute)instruction;
                if (match(v.getValueref())) {
                    v.setValueref(newInstruction(v.getValueref()));
                } else {
                    visit(v.getValueref());
                }
            }
            break;
        case ByteCodeConstants.CONVERT,
             ByteCodeConstants.IMPLICITCONVERT:
            {
                ConvertInstruction ci = (ConvertInstruction)instruction;
                if (match(ci.getValue())) {
                    ci.setValue(newInstruction(ci.getValue()));
                } else {
                    visit(ci.getValue());
                }
            }
            break;
        case ByteCodeConstants.IFCMP:
            {
                IfCmp ifCmp = (IfCmp)instruction;
                if (match(ifCmp.getValue1())) {
                    ifCmp.setValue1(newInstruction(ifCmp.getValue1()));
                } else {
                    visit(ifCmp.getValue1());
                }
                if (match(ifCmp.getValue2())) {
                    ifCmp.setValue2(newInstruction(ifCmp.getValue2()));
                } else {
                    visit(ifCmp.getValue2());
                }
            }
            break;
        case ByteCodeConstants.IF,
             ByteCodeConstants.IFXNULL:
            {
                IfInstruction iff = (IfInstruction)instruction;
                if (match(iff.getValue())) {
                    iff.setValue(newInstruction(iff.getValue()));
                } else {
                    visit(iff.getValue());
                }
            }
            break;
        case Const.INVOKEINTERFACE,
             Const.INVOKESPECIAL,
             Const.INVOKEVIRTUAL:
            {
                InvokeNoStaticInstruction insi =
                    (InvokeNoStaticInstruction)instruction;
                if (match(insi.getObjectref())) {
                    insi.setObjectref(newInstruction(insi.getObjectref()));
                } else {
                    visit(insi.getObjectref());
                }
            }
            // intended fall through
        case Const.INVOKESTATIC,
             ByteCodeConstants.INVOKENEW:
            {
                List<Instruction> list = ((InvokeInstruction)instruction).getArgs();
                for (int i=list.size()-1; i>=0; --i)
                {
                    if (match(list.get(i))) {
                        list.set(i, newInstruction(list.get(i)));
                    } else {
                        visit(list.get(i));
                    }
                }
            }
            break;
        case Const.LOOKUPSWITCH, Const.TABLESWITCH:
            {
                Switch ls = (Switch)instruction;
                if (match(ls.getKey())) {
                    ls.setKey(newInstruction(ls.getKey()));
                } else {
                    visit(ls.getKey());
                }
            }
            break;
        case Const.MULTIANEWARRAY:
            {
                List<Instruction> dimensions = ((MultiANewArray)instruction).getDimensions();
                for (int i=dimensions.size()-1; i>=0; --i)
                {
                    if (match(dimensions.get(i))) {
                        dimensions.set(i, newInstruction(dimensions.get(i)));
                    } else {
                        visit(dimensions.get(i));
                    }
                }
            }
            break;
        case Const.NEWARRAY:
            {
                NewArray newArray = (NewArray)instruction;
                if (match(newArray.getDimension())) {
                    newArray.setDimension(newInstruction(newArray.getDimension()));
                } else {
                    visit(newArray.getDimension());
                }
            }
            break;
        case Const.ANEWARRAY:
            {
                ANewArray aNewArray = (ANewArray)instruction;
                if (match(aNewArray.getDimension())) {
                    aNewArray.setDimension(newInstruction(aNewArray.getDimension()));
                } else {
                    visit(aNewArray.getDimension());
                }
            }
            break;
        case Const.PUTFIELD:
            {
                PutField putField = (PutField)instruction;
                if (match(putField.getObjectref())) {
                    putField.setObjectref(newInstruction(putField.getObjectref()));
                } else {
                    visit(putField.getObjectref());
                }
                if (match(putField.getValueref())) {
                    putField.setValueref(newInstruction(putField.getValueref()));
                } else {
                    visit(putField.getValueref());
                }
            }
            break;
        case ByteCodeConstants.ARRAYLOAD:
            {
                ArrayLoadInstruction ali = (ArrayLoadInstruction)instruction;
                if (match(ali.getArrayref())) {
                    ali.setArrayref(newInstruction(ali.getArrayref()));
                } else {
                    visit(ali.getArrayref());
                }
                if (match(ali.getIndexref())) {
                    ali.setIndexref(newInstruction(ali.getIndexref()));
                } else {
                    visit(ali.getIndexref());
                }
            }
            break;
        case Const.ACONST_NULL,
             ByteCodeConstants.LOAD,
             Const.ALOAD,
             Const.ILOAD,
             Const.BIPUSH,
             ByteCodeConstants.ICONST,
             ByteCodeConstants.LCONST,
             ByteCodeConstants.FCONST,
             ByteCodeConstants.DCONST,
             ByteCodeConstants.DUPLOAD,
             ByteCodeConstants.POSTINC,
             ByteCodeConstants.PREINC,
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
                    "Can not replace OuterReference in " +
                    instruction.getClass().getName() +
                    ", opcode=" + instruction.getOpcode());
        }
    }

    public void visit(List<Instruction> instructions)
    {
        for (int idx=instructions.size()-1; idx>=0; --idx)
        {
            Instruction i = instructions.get(idx);

            if (match(i)) {
                instructions.set(idx, newInstruction(i));
            } else {
                visit(i);
            }
        }
    }

    private boolean match(Instruction i)
    {
        return
            i.getOpcode() == this.opcode &&
            ((IndexInstruction)i).getIndex() == this.index;
    }

    private Instruction newInstruction(Instruction i)
    {
        return new GetStatic(
            ByteCodeConstants.OUTERTHIS, i.getOffset(),
            i.getLineNumber(), this.outerThisInstructionIndex);
    }
}
