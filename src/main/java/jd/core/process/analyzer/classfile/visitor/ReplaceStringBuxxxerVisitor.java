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
import org.jd.core.v1.util.StringConstants;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ArrayLength;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.AssertInstruction;
import jd.core.model.instruction.bytecode.instruction.AssignmentInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.ConvertInstruction;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;
import jd.core.model.instruction.bytecode.instruction.MultiANewArray;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.Pop;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.Switch;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.attribute.ObjectrefAttribute;
import jd.core.model.instruction.bytecode.instruction.attribute.ValuerefAttribute;
import jd.core.model.instruction.fast.FastConstants;
import jd.core.model.instruction.fast.instruction.FastSynchronized;
import jd.core.model.instruction.fast.instruction.FastTry;
import jd.core.model.instruction.fast.instruction.FastTry.FastCatch;
import jd.core.util.SignatureUtil;

public class ReplaceStringBuxxxerVisitor
{
    private final ClassFile classFile;
    private final LocalVariables localVariables;

    public ReplaceStringBuxxxerVisitor(ClassFile classFile, LocalVariables localVariables)
    {
        this.classFile = classFile;
        this.localVariables = localVariables;
    }

    public void visit(Instruction instruction)
    {
        switch (instruction.getOpcode())
        {
        case Const.ARRAYLENGTH:
            {
                ArrayLength al = (ArrayLength)instruction;
                Instruction i = match(al.getArrayref());
                if (i == null) {
                    visit(al.getArrayref());
                } else {
                    al.setArrayref(i);
                }
            }
            break;
        case ByteCodeConstants.ARRAYLOAD:
            {
                ArrayLoadInstruction ali = (ArrayLoadInstruction)instruction;
                Instruction i = match(ali.getArrayref());
                if (i == null) {
                    visit(ali.getArrayref());
                } else {
                    ali.setArrayref(i);
                }

                i = match(ali.getIndexref());
                if (i == null) {
                    visit(ali.getIndexref());
                } else {
                    ali.setIndexref(i);
                }
            }
            break;
        case Const.AASTORE,
             ByteCodeConstants.ARRAYSTORE:
            {
                ArrayStoreInstruction asi = (ArrayStoreInstruction)instruction;
                Instruction i = match(asi.getArrayref());
                if (i == null) {
                    visit(asi.getArrayref());
                } else {
                    asi.setArrayref(i);
                }

                i = match(asi.getIndexref());
                if (i == null) {
                    visit(asi.getIndexref());
                } else {
                    asi.setIndexref(i);
                }

                i = match(asi.getValueref());
                if (i == null) {
                    visit(asi.getValueref());
                } else {
                    asi.setValueref(i);
                }
            }
            break;
        case ByteCodeConstants.ASSERT:
            {
                AssertInstruction ai = (AssertInstruction)instruction;
                Instruction i = match(ai.getTest());
                if (i == null) {
                    visit(ai.getTest());
                } else {
                    ai.setTest(i);
                }
                if (ai.getMsg() != null)
                {
                    i = match(ai.getMsg());
                    if (i == null) {
                        visit(ai.getMsg());
                    } else {
                        ai.setMsg(i);
                    }
                }
            }
            break;
        case ByteCodeConstants.ASSIGNMENT:
            {
                AssignmentInstruction ai = (AssignmentInstruction)instruction;
                Instruction i = match(ai.getValue1());
                if (i == null) {
                    visit(ai.getValue1());
                } else {
                    ai.setValue1(i);
                }

                i = match(ai.getValue2());
                if (i == null) {
                    visit(ai.getValue2());
                } else {
                    ai.setValue2(i);
                }
            }
            break;
        case Const.ATHROW:
            {
                AThrow aThrow = (AThrow)instruction;
                visit(aThrow.getValue());
            }
            break;
        case ByteCodeConstants.BINARYOP:
            {
                BinaryOperatorInstruction boi =
                    (BinaryOperatorInstruction)instruction;
                Instruction i = match(boi.getValue1());
                if (i == null) {
                    visit(boi.getValue1());
                } else {
                    boi.setValue1(i);
                }

                i = match(boi.getValue2());
                if (i == null) {
                    visit(boi.getValue2());
                } else {
                    boi.setValue2(i);
                }
            }
            break;
        case ByteCodeConstants.UNARYOP:
            {
                UnaryOperatorInstruction uoi =
                    (UnaryOperatorInstruction)instruction;
                Instruction i = match(uoi.getValue());
                if (i == null) {
                    visit(uoi.getValue());
                } else {
                    uoi.setValue(i);
                }
            }
            break;
        case ByteCodeConstants.DUPSTORE,
             ByteCodeConstants.TERNARYOPSTORE,
             Const.CHECKCAST,
             Const.GETFIELD,
             Const.MONITORENTER,
             Const.MONITOREXIT,
             Const.INSTANCEOF:
            {
                ObjectrefAttribute o = (ObjectrefAttribute)instruction;
                Instruction i = match(o.getObjectref());
                if (i == null) {
                    visit(o.getObjectref());
                } else {
                    o.setObjectref(i);
                }
            }
            break;
        case ByteCodeConstants.CONVERT,
             ByteCodeConstants.IMPLICITCONVERT:
            {
                ConvertInstruction ci = (ConvertInstruction)instruction;
                Instruction i = match(ci.getValue());
                if (i == null) {
                    visit(ci.getValue());
                } else {
                    ci.setValue(i);
                }
            }
            break;
        case ByteCodeConstants.IF,
             ByteCodeConstants.IFXNULL:
            {
                IfInstruction ifInstruction = (IfInstruction)instruction;
                Instruction i = match(ifInstruction.getValue());
                if (i == null) {
                    visit(ifInstruction.getValue());
                } else {
                    ifInstruction.setValue(i);
                }
            }
            break;
        case ByteCodeConstants.IFCMP:
            {
                IfCmp ifCmpInstruction = (IfCmp)instruction;
                Instruction i = match(ifCmpInstruction.getValue1());
                if (i == null) {
                    visit(ifCmpInstruction.getValue1());
                } else {
                    ifCmpInstruction.setValue1(i);
                }

                i = match(ifCmpInstruction.getValue2());
                if (i == null) {
                    visit(ifCmpInstruction.getValue2());
                } else {
                    ifCmpInstruction.setValue2(i);
                }
            }
            break;
        case ByteCodeConstants.COMPLEXIF:
            {
                ComplexConditionalBranchInstruction complexIf = (ComplexConditionalBranchInstruction)instruction;
                visit(complexIf.getInstructions());
            }
            break;
        case Const.INVOKEVIRTUAL,
             Const.INVOKEINTERFACE,
             Const.INVOKESPECIAL:
            {
                InvokeNoStaticInstruction insi =
                    (InvokeNoStaticInstruction)instruction;
                Instruction i = match(insi.getObjectref());
                if (i == null) {
                    visit(insi.getObjectref());
                } else {
                    insi.setObjectref(i);
                }
                replaceInArgs(insi.getArgs());
            }
            break;
        case Const.INVOKESTATIC,
             ByteCodeConstants.INVOKENEW:
            replaceInArgs(((InvokeInstruction)instruction).getArgs());
            break;
        case Const.LOOKUPSWITCH, Const.TABLESWITCH:
            {
                Switch sw = (Switch)instruction;
                Instruction i = match(sw.getKey());
                if (i == null) {
                    visit(sw.getKey());
                } else {
                    sw.setKey(i);
                }
            }
            break;
        case Const.MULTIANEWARRAY:
            {
                MultiANewArray multiANewArray = (MultiANewArray)instruction;
                List<Instruction> dimensions = multiANewArray.getDimensions();
                Instruction ins;

                for (int i=dimensions.size()-1; i>=0; i--)
                {
                    ins = match(dimensions.get(i));
                    if (ins == null) {
                        visit(dimensions.get(i));
                    } else {
                        dimensions.set(i, ins);
                    }
                }
            }
            break;
        case Const.NEWARRAY:
            {
                NewArray newArray = (NewArray)instruction;
                Instruction i = match(newArray.getDimension());
                if (i == null) {
                    visit(newArray.getDimension());
                } else {
                    newArray.setDimension(i);
                }
            }
            break;
        case Const.ANEWARRAY:
            {
                ANewArray newArray = (ANewArray)instruction;
                Instruction i = match(newArray.getDimension());
                if (i == null) {
                    visit(newArray.getDimension());
                } else {
                    newArray.setDimension(i);
                }
            }
            break;
        case Const.POP:
            visit(((Pop)instruction).getObjectref());
            break;
        case Const.PUTFIELD:
            {
                PutField putField = (PutField)instruction;
                Instruction i = match(putField.getObjectref());
                if (i == null) {
                    visit(putField.getObjectref());
                } else {
                    putField.setObjectref(i);
                }

                i = match(putField.getValueref());
                if (i == null) {
                    visit(putField.getValueref());
                } else {
                    putField.setValueref(i);
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
                Instruction i = match(v.getValueref());
                if (i == null) {
                    visit(v.getValueref());
                } else {
                    v.setValueref(i);
                }
            }
            break;
        case ByteCodeConstants.TERNARYOP:
            {
                TernaryOperator to = (TernaryOperator)instruction;
                Instruction i = match(to.getValue1());
                if (i == null) {
                    visit(to.getValue1());
                } else {
                    to.setValue1(i);
                }

                i = match(to.getValue2());
                if (i == null) {
                    visit(to.getValue2());
                } else {
                    to.setValue2(i);
                }
            }
            break;
        case ByteCodeConstants.INITARRAY,
             ByteCodeConstants.NEWANDINITARRAY:
            {
                InitArrayInstruction iaInstruction =
                    (InitArrayInstruction)instruction;
                Instruction i = match(iaInstruction.getNewArray());
                if (i == null) {
                    visit(iaInstruction.getNewArray());
                } else {
                    iaInstruction.setNewArray(i);
                }

                for (int index=iaInstruction.getValues().size()-1; index>=0; --index)
                {
                    i = match(iaInstruction.getValues().get(index));
                    if (i == null) {
                        visit(iaInstruction.getValues().get(index));
                    } else {
                        iaInstruction.getValues().set(index, i);
                    }
                }
            }
            break;
        case FastConstants.TRY:
            {
                FastTry ft = (FastTry)instruction;
                visit(ft.getInstructions());
                if (ft.getFinallyInstructions() != null) {
                    visit(ft.getFinallyInstructions());
                }
                List<FastCatch> catchs = ft.getCatches();
                for (int i=catchs.size()-1; i>=0; --i) {
                    visit(catchs.get(i).instructions());
                }
            }
            break;
        case FastConstants.SYNCHRONIZED:
            {
                FastSynchronized fsd = (FastSynchronized)instruction;
                visit(fsd.getMonitor());
                visit(fsd.getInstructions());
            }
            break;
        case Const.ACONST_NULL,
             ByteCodeConstants.DUPLOAD,
             Const.LDC,
             Const.LDC2_W,
             Const.NEW,
             Const.RETURN,
             Const.BIPUSH,
             ByteCodeConstants.DCONST,
             ByteCodeConstants.FCONST,
             ByteCodeConstants.ICONST,
             ByteCodeConstants.LCONST,
             Const.IINC,
             ByteCodeConstants.PREINC,
             ByteCodeConstants.POSTINC,
             Const.JSR,
             Const.GETSTATIC,
             ByteCodeConstants.OUTERTHIS,
             Const.SIPUSH,
             ByteCodeConstants.LOAD,
             Const.ALOAD,
             Const.ILOAD,
             Const.GOTO,
             Const.INVOKEDYNAMIC,
             ByteCodeConstants.EXCEPTIONLOAD,
             Const.RET,
             ByteCodeConstants.RETURNADDRESSLOAD:
            break;
        default:
            System.err.println(
                    "Can not replace StringBuxxxer in " +
                    instruction.getClass().getName() + " " +
                    instruction.getOpcode());
        }
    }

    private void visit(List<Instruction> instructions) {
        instructions.forEach(this::visit);
    }

    private void replaceInArgs(List<Instruction> args)
    {
        if (!args.isEmpty())
        {
            Instruction ins;

            for (int i=args.size()-1; i>=0; --i)
            {
                ins = match(args.get(i));
                if (ins == null) {
                    visit(args.get(i));
                } else {
                    args.set(i, ins);
                }
            }
        }
    }

    private Instruction match(Instruction i)
    {
        if (i.getOpcode() == Const.INVOKEVIRTUAL)
        {
            Invokevirtual iv = (Invokevirtual)i;
            
            ConstantPool constants = classFile.getConstantPool();
            ConstantCP cmr = constants .getConstantMethodref(iv.getIndex());
            ConstantClass cc = constants.getConstantClass(cmr.getClassIndex());

            if (cc.getNameIndex() == constants.getStringBufferClassNameIndex() ||
                cc.getNameIndex() == constants.getStringBuilderClassNameIndex())
            {
                ConstantNameAndType cnat =
                    constants.getConstantNameAndType(cmr.getNameAndTypeIndex());

                if (cnat.getNameIndex() == constants.getToStringIndex()) {
                    SignatureInfo signatureInfo = new SignatureInfo();
                    Instruction result = match(iv.getObjectref(), cmr.getClassIndex(), signatureInfo);
                    if (signatureInfo.accept()) {
                        return result;
                    }
                }
            }
        }

        return null;
    }

    private Instruction match(Instruction i, int classIndex, SignatureInfo signatureInfo)
    {
        ConstantPool constants = classFile.getConstantPool();
        if (i.getOpcode() == Const.INVOKEVIRTUAL)
        {
            InvokeNoStaticInstruction insi = (InvokeNoStaticInstruction)i;
            ConstantCP cmr = constants .getConstantMethodref(insi.getIndex());

            if (cmr.getClassIndex() == classIndex)
            {
                ConstantNameAndType cnat =
                    constants.getConstantNameAndType(cmr.getNameAndTypeIndex());

                if (cnat.getNameIndex() == constants.getAppendIndex() &&
                    insi.getArgs().size() == 1)
                {
                    String descriptor = constants.getConstantUtf8(cnat.getSignatureIndex());
                    List<String> parameterSignatures = SignatureUtil.getParameterSignatures(descriptor);
                    signatureInfo.appendSignatures.add(parameterSignatures.get(0));
                    Instruction result = match(insi.getObjectref(), cmr.getClassIndex(), signatureInfo);

                    if (result == null)
                    {
                        return insi.getArgs().get(0);
                    }
                    return new BinaryOperatorInstruction(
                        ByteCodeConstants.BINARYOP, i.getOffset(), i.getLineNumber(),
                        4,  StringConstants.INTERNAL_STRING_SIGNATURE, "+",
                        result, insi.getArgs().get(0));
                }
            }
        }
        else if (i.getOpcode() == ByteCodeConstants.INVOKENEW)
        {
            InvokeNew in = (InvokeNew)i;
            ConstantCP cmr = constants.getConstantMethodref(in.getIndex());

            if (cmr.getClassIndex() == classIndex && in.getArgs().size() == 1)
            {
                Instruction arg0 = in.getArgs().get(0);

                // Remove String.valueOf for String
                if (arg0.getOpcode() == Const.INVOKESTATIC)
                {
                    Invokestatic is = (Invokestatic)arg0;
                    cmr = constants.getConstantMethodref(is.getIndex());
                    ConstantClass cc = constants.getConstantClass(cmr.getClassIndex());

                    if (cc.getNameIndex() == constants.getStringClassNameIndex())
                    {
                        ConstantNameAndType cnat =
                            constants.getConstantNameAndType(cmr.getNameAndTypeIndex());

                        if (cnat.getNameIndex() == constants.getValueOfIndex() &&
                            is.getArgs().size() == 1) {
                            return is.getArgs().get(0);
                        }
                    }
                }

                signatureInfo.invokeNewSignature = arg0.getReturnedSignature(classFile, localVariables);
                return arg0;
            }
        }

        return null;
    }

    private static class SignatureInfo {
        private String invokeNewSignature;
        private Set<String> appendSignatures = new HashSet<>();

        private boolean accept() {
            return !"I".equals(invokeNewSignature)
                    && appendSignatures.contains(StringConstants.INTERNAL_STRING_SIGNATURE);
        }
    }
}
