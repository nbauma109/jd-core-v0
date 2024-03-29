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
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantNameAndType;

import java.util.List;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ArrayLength;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.AssertInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.ConvertInstruction;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;
import jd.core.model.instruction.bytecode.instruction.MultiANewArray;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.Switch;
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.attribute.ObjectrefAttribute;
import jd.core.model.instruction.bytecode.instruction.attribute.ValuerefAttribute;
import jd.core.util.SignatureUtil;

/*
 * Search :
        public int indexOf(int ch)
        public int indexOf(int ch, int fromIndex)
        public int lastIndexOf(int ch)
        public int lastIndexOf(int ch, int fromIndex)
 */
public class SetConstantTypeInStringIndexOfMethodsVisitor
{
    private final ConstantPool constants;

    public SetConstantTypeInStringIndexOfMethodsVisitor(ConstantPool constants)
    {
        this.constants = constants;
    }

    public void visit(Instruction instruction)
    {
        switch (instruction.getOpcode())
        {
        case Const.ARRAYLENGTH:
            visit(((ArrayLength)instruction).getArrayref());
            break;
        case Const.AASTORE,
             ByteCodeConstants.ARRAYSTORE:
            visit(((ArrayStoreInstruction)instruction).getArrayref());
            break;
        case ByteCodeConstants.ASSERT:
            {
                AssertInstruction ai = (AssertInstruction)instruction;
                visit(ai.getTest());
                if (ai.getMsg() != null) {
                    visit(ai.getMsg());
                }
            }
            break;
        case Const.ATHROW:
            visit(((AThrow)instruction).getValue());
            break;
        case ByteCodeConstants.UNARYOP:
            visit(((UnaryOperatorInstruction)instruction).getValue());
            break;
        case ByteCodeConstants.BINARYOP,
             ByteCodeConstants.ASSIGNMENT:
            {
                BinaryOperatorInstruction boi =
                    (BinaryOperatorInstruction)instruction;
                visit(boi.getValue1());
                visit(boi.getValue2());
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
            visit(((ObjectrefAttribute)instruction).getObjectref());
            break;
        case ByteCodeConstants.STORE,
             ByteCodeConstants.XRETURN,
             Const.ASTORE,
             Const.ISTORE,
             Const.PUTSTATIC:
            visit(((ValuerefAttribute)instruction).getValueref());
            break;
        case ByteCodeConstants.CONVERT,
             ByteCodeConstants.IMPLICITCONVERT:
            visit(((ConvertInstruction)instruction).getValue());
            break;
        case ByteCodeConstants.IFCMP:
            {
                IfCmp ifCmp = (IfCmp)instruction;
                visit(ifCmp.getValue1());
                visit(ifCmp.getValue2());
            }
            break;
        case ByteCodeConstants.IF,
             ByteCodeConstants.IFXNULL:
            visit(((IfInstruction)instruction).getValue());
            break;
        case ByteCodeConstants.COMPLEXIF:
            {
                visit(((ComplexConditionalBranchInstruction)instruction).getInstructions());
            }
            break;
        case Const.INVOKEVIRTUAL:
            {
                Invokevirtual iv = (Invokevirtual)instruction;
                ConstantCP cmr =
                    this.constants.getConstantMethodref(iv.getIndex());
                ConstantClass cc = this.constants.getConstantClass(cmr.getClassIndex());

                if (cc.getNameIndex() == this.constants.getStringClassNameIndex())
                {
                    int nbrOfParameters = iv.getArgs().size();

                    if (1 <= nbrOfParameters && nbrOfParameters <= 2)
                    {
                        int opcode = iv.getArgs().get(0).getOpcode();

                        ConstantNameAndType cnat = constants.getConstantNameAndType(
                                cmr.getNameAndTypeIndex());

                        String methodDescriptor =
                            constants.getConstantUtf8(cnat.getSignatureIndex());

                        String methodReturnedSignature = SignatureUtil.getMethodReturnedSignature(methodDescriptor);
                        List<String> parameterSignatures = SignatureUtil.getParameterSignatures(methodDescriptor);
                        
                        if ((opcode==Const.BIPUSH ||
                             opcode==Const.SIPUSH) &&
                             "I".equals(methodReturnedSignature) &&
                             "I".equals(parameterSignatures.get(0)))
                        {
                            String name =
                                this.constants.getConstantUtf8(cnat.getNameIndex());

                            if ("indexOf".equals(name) ||
                                "lastIndexOf".equals(name))
                            {
                                // Change constant type
                                IConst ic = (IConst)iv.getArgs().get(0);
                                ic.setReturnedSignature("C");
                                break;
                            }
                        }
                    }
                }
            }
            // intended fall through
        case Const.INVOKEINTERFACE,
             Const.INVOKESPECIAL:
            visit(((InvokeNoStaticInstruction)instruction).getObjectref());
            // intended fall through
        case Const.INVOKESTATIC,
             ByteCodeConstants.INVOKENEW:
            {
                List<Instruction> list = ((InvokeInstruction)instruction).getArgs();
                for (int i=list.size()-1; i>=0; --i) {
                    visit(list.get(i));
                }
            }
            break;
        case Const.LOOKUPSWITCH, Const.TABLESWITCH:
            visit(((Switch)instruction).getKey());
            break;
        case Const.MULTIANEWARRAY:
            {
                visit(((MultiANewArray)instruction).getDimensions());
            }
            break;
        case Const.NEWARRAY:
            visit(((NewArray)instruction).getDimension());
            break;
        case Const.ANEWARRAY:
            visit(((ANewArray)instruction).getDimension());
            break;
        case Const.PUTFIELD:
            {
                PutField putField = (PutField)instruction;
                visit(putField.getObjectref());
                visit(putField.getValueref());
            }
            break;
        case ByteCodeConstants.PREINC,
             ByteCodeConstants.POSTINC:
            visit(((IncInstruction)instruction).getValue());
            break;
        case ByteCodeConstants.INITARRAY,
             ByteCodeConstants.NEWANDINITARRAY:
            {
                InitArrayInstruction iai = (InitArrayInstruction)instruction;
                visit(iai.getNewArray());
                if (iai.getValues() != null) {
                    visit(iai.getValues());
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
                    "Can not search String.indexOf in " +
                    instruction.getClass().getName() +
                    ", opcode=" + instruction.getOpcode());
        }
    }

    public void visit(List<Instruction> instructions)
    {
        for (int i=instructions.size()-1; i>=0; --i) {
            visit(instructions.get(i));
        }
    }
}
