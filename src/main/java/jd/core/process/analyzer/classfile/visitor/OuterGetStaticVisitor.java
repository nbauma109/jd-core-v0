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
import org.apache.bcel.classfile.ConstantNameAndType;

import java.util.List;
import java.util.Map;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.accessor.Accessor;
import jd.core.model.classfile.accessor.AccessorConstants;
import jd.core.model.classfile.accessor.GetStaticAccessor;
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
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.model.instruction.bytecode.instruction.MultiANewArray;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.Switch;
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.attribute.ObjectrefAttribute;
import jd.core.model.instruction.bytecode.instruction.attribute.ValuerefAttribute;

/*
 * Replace 'EntitlementFunctionLibrary.access$000()'
 * par 'EntitlementFunctionLibrary.kernelId'
 */
public class OuterGetStaticVisitor
{
    protected final Map<String, ClassFile> innerClassesMap;
    protected final ConstantPool constants;

    public OuterGetStaticVisitor(
        Map<String, ClassFile> innerClassesMap, ConstantPool constants)
    {
        this.innerClassesMap = innerClassesMap;
        this.constants = constants;
    }

    public void visit(Instruction instruction)
    {
        switch (instruction.getOpcode())
        {
        case Const.ARRAYLENGTH:
            {
                ArrayLength al = (ArrayLength)instruction;
                Accessor a = match(al.getArrayref());
                if (a != null) {
                    al.setArrayref(newInstruction(al.getArrayref(), a));
                } else {
                    visit(al.getArrayref());
                }
            }
            break;
        case Const.AASTORE,
             ByteCodeConstants.ARRAYSTORE:
            {
                ArrayStoreInstruction asi = (ArrayStoreInstruction)instruction;
                Accessor a = match(asi.getArrayref());
                if (a != null) {
                    asi.setArrayref(newInstruction(asi.getArrayref(), a));
                } else {
                    visit(asi.getArrayref());
                }
                a = match(asi.getIndexref());
                if (a != null) {
                    asi.setIndexref(newInstruction(asi.getIndexref(), a));
                } else {
                    visit(asi.getIndexref());
                }
                a = match(asi.getValueref());
                if (a != null) {
                    asi.setValueref(newInstruction(asi.getValueref(), a));
                } else {
                    visit(asi.getValueref());
                }
            }
            break;
        case Const.ATHROW:
            {
                AThrow aThrow = (AThrow)instruction;
                Accessor a = match(aThrow.getValue());
                if (a != null) {
                    aThrow.setValue(newInstruction(aThrow.getValue(), a));
                } else {
                    visit(aThrow.getValue());
                }
            }
            break;
        case ByteCodeConstants.UNARYOP:
            {
                UnaryOperatorInstruction uoi = (UnaryOperatorInstruction)instruction;
                Accessor a = match(uoi.getValue());
                if (a != null) {
                    uoi.setValue(newInstruction(uoi.getValue(), a));
                } else {
                    visit(uoi.getValue());
                }
            }
            break;
        case ByteCodeConstants.BINARYOP:
            {
                BinaryOperatorInstruction boi = (BinaryOperatorInstruction)instruction;
                Accessor a = match(boi.getValue1());
                if (a != null) {
                    boi.setValue1(newInstruction(boi.getValue1(), a));
                } else {
                    visit(boi.getValue1());
                }
                a = match(boi.getValue2());
                if (a != null) {
                    boi.setValue2(newInstruction(boi.getValue2(), a));
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
                visitObjectref(instruction);
            }
            break;
        case ByteCodeConstants.STORE,
             ByteCodeConstants.XRETURN,
             Const.ASTORE,
             Const.ISTORE,
             Const.PUTSTATIC:
            {
                ValuerefAttribute v = (ValuerefAttribute)instruction;
                Accessor a = match(v.getValueref());
                if (a != null) {
                    v.setValueref(newInstruction(v.getValueref(), a));
                }
                visit(v.getValueref());
            }
            break;
        case ByteCodeConstants.CONVERT,
             ByteCodeConstants.IMPLICITCONVERT:
            {
                ConvertInstruction ci = (ConvertInstruction)instruction;
                Accessor a = match(ci.getValue());
                if (a != null) {
                    ci.setValue(newInstruction(ci.getValue(), a));
                } else {
                    visit(ci.getValue());
                }
            }
            break;
        case ByteCodeConstants.IFCMP:
            {
                IfCmp ifCmp = (IfCmp)instruction;
                Accessor a = match(ifCmp.getValue1());
                if (a != null) {
                    ifCmp.setValue1(newInstruction(ifCmp.getValue1(), a));
                }
                visit(ifCmp.getValue1());
                a = match(ifCmp.getValue2());
                if (a != null) {
                    ifCmp.setValue2(newInstruction(ifCmp.getValue2(), a));
                }
                visit(ifCmp.getValue2());
            }
            break;
        case ByteCodeConstants.IF,
             ByteCodeConstants.IFXNULL:
            {
                IfInstruction iff = (IfInstruction)instruction;
                Accessor a = match(iff.getValue());
                if (a != null) {
                    iff.setValue(newInstruction(iff.getValue(), a));
                } else {
                    visit(iff.getValue());
                }
            }
            break;
        case Const.INVOKEINTERFACE,
             Const.INVOKESPECIAL,
             Const.INVOKEVIRTUAL:
            {
                visitObjectref(instruction);
            }
            // intended fall through
        case Const.INVOKESTATIC,
             ByteCodeConstants.INVOKENEW:
            {
                List<Instruction> list = ((InvokeInstruction)instruction).getArgs();
                for (int i=list.size()-1; i>=0; --i)
                {
                    Accessor a = match(list.get(i));
                    if (a != null) {
                        list.set(i, newInstruction(list.get(i), a));
                    } else {
                        visit(list.get(i));
                    }
                }
            }
            break;
        case Const.LOOKUPSWITCH, Const.TABLESWITCH:
            {
                Switch sw = (Switch)instruction;
                Accessor a = match(sw.getKey());
                if (a != null) {
                    sw.setKey(newInstruction(sw.getKey(), a));
                } else {
                    visit(sw.getKey());
                }
            }
            break;
        case Const.MULTIANEWARRAY:
            {
                List<Instruction> dimensions = ((MultiANewArray)instruction).getDimensions();
                for (int i=dimensions.size()-1; i>=0; --i)
                {
                    Accessor a = match(dimensions.get(i));
                    if (a != null) {
                        dimensions.set(i, newInstruction(dimensions.get(i), a));
                    } else {
                        visit(dimensions.get(i));
                    }
                }
            }
            break;
        case Const.NEWARRAY:
            {
                NewArray newArray = (NewArray)instruction;
                Accessor a = match(newArray.getDimension());
                if (a != null) {
                    newArray.setDimension(newInstruction(newArray.getDimension(), a));
                } else {
                    visit(newArray.getDimension());
                }
            }
            break;
        case Const.ANEWARRAY:
            {
                ANewArray aNewArray = (ANewArray)instruction;
                Accessor a = match(aNewArray.getDimension());
                if (a != null) {
                    aNewArray.setDimension(newInstruction(aNewArray.getDimension(), a));
                } else {
                    visit(aNewArray.getDimension());
                }
            }
            break;
        case Const.PUTFIELD:
            {
                PutField putField = (PutField)instruction;
                Accessor a = match(putField.getObjectref());
                if (a != null) {
                    putField.setObjectref(newInstruction(putField.getObjectref(), a));
                } else {
                    visit(putField.getObjectref());
                }
                a = match(putField.getValueref());
                if (a != null) {
                    putField.setValueref(newInstruction(putField.getValueref(), a));
                } else {
                    visit(putField.getValueref());
                }
            }
            break;
        case ByteCodeConstants.ARRAYLOAD:
            {
                ArrayLoadInstruction ali = (ArrayLoadInstruction)instruction;
                Accessor a = match(ali.getArrayref());
                if (a != null) {
                    ali.setArrayref(newInstruction(ali.getArrayref(), a));
                } else {
                    visit(ali.getArrayref());
                }
                a = match(ali.getIndexref());
                if (a != null) {
                    ali.setIndexref(newInstruction(ali.getIndexref(), a));
                } else {
                    visit(ali.getIndexref());
                }
            }
            break;
        case ByteCodeConstants.PREINC,
             ByteCodeConstants.POSTINC:
            {
                IncInstruction ii = (IncInstruction)instruction;
                Accessor a = match(ii.getValue());
                if (a != null) {
                    ii.setValue(newInstruction(ii.getValue(), a));
                } else {
                    visit(ii.getValue());
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
                    "Can not replace accessor in " +
                    instruction.getClass().getName() +
                    ", opcode=" + instruction.getOpcode());
        }
    }

    private void visitObjectref(Instruction instruction) {
        ObjectrefAttribute o = (ObjectrefAttribute)instruction;
        Accessor a = match(o.getObjectref());
        if (a != null) {
            o.setObjectref(newInstruction(o.getObjectref(), a));
        } else {
            visit(o.getObjectref());
        }
    }

    public void visit(List<Instruction> instructions)
    {
        for (int index=instructions.size()-1; index>=0; --index)
        {
            Instruction i = instructions.get(index);
            visit(i);
            Accessor a = match(i);
            if (a != null) {
                instructions.set(index, newInstruction(i, a));
            }

        }
    }

    protected Accessor match(Instruction i)
    {
        if (i.getOpcode() != Const.INVOKESTATIC) {
            return null;
        }

        Invokestatic is = (Invokestatic)i;
        ConstantCP cmr = constants.getConstantMethodref(is.getIndex());
        ConstantNameAndType cnat =
            constants.getConstantNameAndType(cmr.getNameAndTypeIndex());
        String descriptor =
            constants.getConstantUtf8(cnat.getSignatureIndex());

        // Zero parameter ?
        if (descriptor.charAt(1) != ')') {
            return null;
        }

        String className = constants.getConstantClassName(cmr.getClassIndex());
        ClassFile classFile = this.innerClassesMap.get(className);
        if (classFile == null) {
            return null;
        }

        String name =
            constants.getConstantUtf8(cnat.getNameIndex());

        Accessor accessor = classFile.getAccessor(name, descriptor);

        if (accessor == null ||
            accessor.tag() != getTargetTag()) {
            return null;
        }

        return accessor;
    }

    protected byte getTargetTag()
    {
        return AccessorConstants.ACCESSOR_GETSTATIC;
    }

    protected Instruction newInstruction(Instruction i, Accessor a)
    {
        GetStaticAccessor gsa = (GetStaticAccessor)a;

        int nameIndex = this.constants.addConstantUtf8(gsa.fieldName());
        int descriptorIndex =
            this.constants.addConstantUtf8(gsa.fieldDescriptor());
        int cnatIndex =
            this.constants.addConstantNameAndType(nameIndex, descriptorIndex);

        int classNameIndex = this.constants.addConstantUtf8(gsa.className());
        int classIndex = this.constants.addConstantClass(classNameIndex);

        int cfrIndex =
            constants.addConstantFieldref(classIndex, cnatIndex);

        return new GetStatic(
            Const.GETSTATIC, i.getOffset(), i.getLineNumber(), cfrIndex);
    }
}
