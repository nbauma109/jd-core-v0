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
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantNameAndType;

import java.util.List;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Method;
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
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.model.instruction.bytecode.instruction.MultiANewArray;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.Switch;
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.attribute.ObjectrefAttribute;
import jd.core.model.instruction.bytecode.instruction.attribute.ValuerefAttribute;
import jd.core.util.SignatureUtil;

/*
 * Replace static call to "OuterClass access$0(InnerClass)" methods.
 */
public class ReplaceOuterAccessorVisitor
{
    protected final ClassFile classFile;

    public ReplaceOuterAccessorVisitor(ClassFile classFile)
    {
        this.classFile = classFile;
    }

    public void visit(Instruction instruction)
    {
        switch (instruction.getOpcode())
        {
        case Const.ARRAYLENGTH:
            {
                ArrayLength al = (ArrayLength)instruction;
                ClassFile matchedClassFile = match(al.getArrayref());
                if (matchedClassFile != null) {
                    al.setArrayref(newInstruction(matchedClassFile, al.getArrayref()));
                } else {
                    visit(al.getArrayref());
                }
            }
            break;
        case Const.AASTORE,
             ByteCodeConstants.ARRAYSTORE:
            {
                ArrayStoreInstruction asi = (ArrayStoreInstruction)instruction;
                ClassFile matchedClassFile = match(asi.getArrayref());
                if (matchedClassFile != null) {
                    asi.setArrayref(newInstruction(matchedClassFile, asi.getArrayref()));
                } else {
                    visit(asi.getArrayref());
                }
                matchedClassFile = match(asi.getIndexref());
                if (matchedClassFile != null) {
                    asi.setIndexref(newInstruction(matchedClassFile, asi.getIndexref()));
                } else {
                    visit(asi.getIndexref());
                }
                matchedClassFile = match(asi.getValueref());
                if (matchedClassFile != null) {
                    asi.setValueref(newInstruction(matchedClassFile, asi.getValueref()));
                } else {
                    visit(asi.getValueref());
                }
            }
            break;
        case Const.ATHROW:
            {
                AThrow aThrow = (AThrow)instruction;
                ClassFile matchedClassFile = match(aThrow.getValue());
                if (matchedClassFile != null) {
                    aThrow.setValue(newInstruction(matchedClassFile, aThrow.getValue()));
                } else {
                    visit(aThrow.getValue());
                }
            }
            break;
        case ByteCodeConstants.UNARYOP:
            {
                UnaryOperatorInstruction uoi = (UnaryOperatorInstruction)instruction;
                ClassFile matchedClassFile = match(uoi.getValue());
                if (matchedClassFile != null) {
                    uoi.setValue(newInstruction(matchedClassFile, uoi.getValue()));
                } else {
                    visit(uoi.getValue());
                }
            }
            break;
        case ByteCodeConstants.BINARYOP:
            {
                BinaryOperatorInstruction boi = (BinaryOperatorInstruction)instruction;
                ClassFile matchedClassFile = match(boi.getValue1());
                if (matchedClassFile != null) {
                    boi.setValue1(newInstruction(matchedClassFile, boi.getValue1()));
                } else {
                    visit(boi.getValue1());
                }
                matchedClassFile = match(boi.getValue2());
                if (matchedClassFile != null) {
                    boi.setValue2(newInstruction(matchedClassFile, boi.getValue2()));
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
                ClassFile matchedClassFile = match(o.getObjectref());
                if (matchedClassFile != null) {
                    o.setObjectref(newInstruction(matchedClassFile, o.getObjectref()));
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
                ClassFile matchedClassFile = match(v.getValueref());
                if (matchedClassFile != null) {
                    v.setValueref(newInstruction(matchedClassFile, v.getValueref()));
                }
                visit(v.getValueref());
            }
            break;
        case ByteCodeConstants.CONVERT,
             ByteCodeConstants.IMPLICITCONVERT:
            {
                ConvertInstruction ci = (ConvertInstruction)instruction;
                ClassFile matchedClassFile = match(ci.getValue());
                if (matchedClassFile != null) {
                    ci.setValue(newInstruction(matchedClassFile, ci.getValue()));
                } else {
                    visit(ci.getValue());
                }
            }
            break;
        case ByteCodeConstants.IFCMP:
            {
                IfCmp ifCmp = (IfCmp)instruction;
                ClassFile matchedClassFile = match(ifCmp.getValue1());
                if (matchedClassFile != null) {
                    ifCmp.setValue1(newInstruction(matchedClassFile, ifCmp.getValue1()));
                } else {
                    visit(ifCmp.getValue1());
                }
                matchedClassFile = match(ifCmp.getValue2());
                if (matchedClassFile != null) {
                    ifCmp.setValue2(newInstruction(matchedClassFile, ifCmp.getValue2()));
                } else {
                    visit(ifCmp.getValue2());
                }
            }
            break;
        case ByteCodeConstants.IF,
             ByteCodeConstants.IFXNULL:
            {
                IfInstruction iff = (IfInstruction)instruction;
                ClassFile matchedClassFile = match(iff.getValue());
                if (matchedClassFile != null) {
                    iff.setValue(newInstruction(matchedClassFile, iff.getValue()));
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
                ClassFile matchedClassFile = match(insi.getObjectref());
                if (matchedClassFile != null) {
                    insi.setObjectref(newInstruction(matchedClassFile, insi.getObjectref()));
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
                    ClassFile matchedClassFile = match(list.get(i));
                    if (matchedClassFile != null) {
                        list.set(i, newInstruction(matchedClassFile, list.get(i)));
                    } else {
                        visit(list.get(i));
                    }
                }
            }
            break;
        case Const.LOOKUPSWITCH, Const.TABLESWITCH:
            {
                Switch ls = (Switch)instruction;
                ClassFile matchedClassFile = match(ls.getKey());
                if (matchedClassFile != null) {
                    ls.setKey(newInstruction(matchedClassFile, ls.getKey()));
                } else {
                    visit(ls.getKey());
                }
            }
            break;
        case Const.MULTIANEWARRAY:
            {
                Instruction[] dimensions = ((MultiANewArray)instruction).getDimensions();
                for (int i=dimensions.length-1; i>=0; --i)
                {
                    ClassFile matchedClassFile = match(dimensions[i]);
                    if (matchedClassFile != null) {
                        dimensions[i] = newInstruction(matchedClassFile, dimensions[i]);
                    } else {
                        visit(dimensions[i]);
                    }
                }
            }
            break;
        case Const.NEWARRAY:
            {
                NewArray newArray = (NewArray)instruction;
                ClassFile matchedClassFile = match(newArray.getDimension());
                if (matchedClassFile != null) {
                    newArray.setDimension(newInstruction(matchedClassFile, newArray.getDimension()));
                } else {
                    visit(newArray.getDimension());
                }
            }
            break;
        case Const.ANEWARRAY:
            {
                ANewArray aNewArray = (ANewArray)instruction;
                ClassFile matchedClassFile = match(aNewArray.getDimension());
                if (matchedClassFile != null) {
                    aNewArray.setDimension(newInstruction(matchedClassFile, aNewArray.getDimension()));
                } else {
                    visit(aNewArray.getDimension());
                }
            }
            break;
        case Const.PUTFIELD:
            {
                PutField putField = (PutField)instruction;
                ClassFile matchedClassFile = match(putField.getObjectref());
                if (matchedClassFile != null) {
                    putField.setObjectref(newInstruction(matchedClassFile, putField.getObjectref()));
                } else {
                    visit(putField.getObjectref());
                }
                matchedClassFile = match(putField.getValueref());
                if (matchedClassFile != null) {
                    putField.setValueref(newInstruction(matchedClassFile, putField.getValueref()));
                } else {
                    visit(putField.getValueref());
                }
            }
            break;
        case ByteCodeConstants.ARRAYLOAD:
            {
                ArrayLoadInstruction ali = (ArrayLoadInstruction)instruction;
                ClassFile matchedClassFile = match(ali.getArrayref());
                if (matchedClassFile != null) {
                    ali.setArrayref(newInstruction(matchedClassFile, ali.getArrayref()));
                } else {
                    visit(ali.getArrayref());
                }
                matchedClassFile = match(ali.getIndexref());
                if (matchedClassFile != null) {
                    ali.setIndexref(newInstruction(matchedClassFile, ali.getIndexref()));
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
                    "Can not replace OuterAccessor in " +
                    instruction.getClass().getName() +
                    ", opcode=" + instruction.getOpcode());
        }
    }

    public void visit(List<Instruction> instructions)
    {
        for (int index=instructions.size()-1; index>=0; --index)
        {
            Instruction i = instructions.get(index);
            ClassFile matchedClassFile = match(i);

            if (matchedClassFile != null) {
                instructions.set(index, newInstruction(matchedClassFile, i));
            } else {
                visit(i);
            }
        }
    }

    protected ClassFile match(Instruction instruction)
    {
        if (instruction.getOpcode() != Const.INVOKESTATIC) {
            return null;
        }

        Invokestatic is = (Invokestatic)instruction;
        if (is.getArgs().size() != 1) {
            return null;
        }

        ClassFile matchedClassFile = innerMatch(is.getArgs().get(0));

        if (matchedClassFile == null || !matchedClassFile.isAInnerClass()) {
            return null;
        }

        ConstantPool constants = classFile.getConstantPool();

        ConstantCP cmr =
            constants.getConstantMethodref(is.getIndex());
        String className =
            constants.getConstantClassName(cmr.getClassIndex());

        if (!className.equals(matchedClassFile.getThisClassName())) {
            return null;
        }

        ConstantNameAndType cnat =
            constants.getConstantNameAndType(cmr.getNameAndTypeIndex());
        String methodName = constants.getConstantUtf8(cnat.getNameIndex());
        String methodDescriptor =
            constants.getConstantUtf8(cnat.getSignatureIndex());
        Method method =
            matchedClassFile.getMethod(methodName, methodDescriptor);

        if (method == null ||
            (method.getAccessFlags() & (Const.ACC_SYNTHETIC|Const.ACC_STATIC))
                != (Const.ACC_SYNTHETIC|Const.ACC_STATIC)) {
            return null;
        }

        ClassFile outerClassFile = matchedClassFile.getOuterClass();
        String returnedSignature = SignatureUtil.getMethodReturnedSignature(methodDescriptor);

        if (!returnedSignature.equals(outerClassFile.getInternalClassName())) {
            return null;
        }

        return outerClassFile;
    }

    private ClassFile innerMatch(Instruction instruction)
    {
        switch (instruction.getOpcode())
        {
        case ByteCodeConstants.OUTERTHIS:
            {
                GetStatic gs = (GetStatic)instruction;
                ConstantPool constants = classFile.getConstantPool();

                ConstantFieldref cfr = constants.getConstantFieldref(gs.getIndex());
                String className =
                    constants.getConstantClassName(cfr.getClassIndex());
                ClassFile outerClassFile = classFile.getOuterClass();

                if (outerClassFile == null ||
                    !className.equals(outerClassFile.getThisClassName())) {
                    return null;
                }

                ConstantNameAndType cnat =
                    constants.getConstantNameAndType(cfr.getNameAndTypeIndex());
                String descriptor =
                    constants.getConstantUtf8(cnat.getSignatureIndex());

                if (! descriptor.equals(outerClassFile.getInternalClassName())) {
                    return null;
                }

                return outerClassFile;
            }
        case Const.INVOKESTATIC:
            return match(instruction);
        default:
            return null;
        }
    }

    private Instruction newInstruction(ClassFile matchedClassFile, Instruction i)
    {
        String internalMatchedClassName =
            matchedClassFile.getInternalClassName();
        String matchedClassName = matchedClassFile.getThisClassName();

        ConstantPool constants = this.classFile.getConstantPool();

        int signatureIndex = constants.addConstantUtf8(matchedClassName);
        int classIndex = constants.addConstantClass(signatureIndex);
        int thisIndex = constants.getThisLocalVariableNameIndex();
        int descriptorIndex =
            constants.addConstantUtf8(internalMatchedClassName);
        int nameAndTypeIndex = constants.addConstantNameAndType(
            thisIndex, descriptorIndex);

        int matchedThisFieldrefIndex =
            constants.addConstantFieldref(classIndex, nameAndTypeIndex);

        return new GetStatic(
            ByteCodeConstants.OUTERTHIS, i.getOffset(),
            i.getLineNumber(), matchedThisFieldrefIndex);
    }
}
