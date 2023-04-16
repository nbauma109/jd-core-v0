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
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.Signature;
import org.jd.core.v1.model.javasyntax.type.BaseType;
import org.jd.core.v1.model.javasyntax.type.BaseTypeArgument;
import org.jd.core.v1.model.javasyntax.type.GenericType;
import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.model.javasyntax.type.Type;
import org.jd.core.v1.model.javasyntax.type.TypeArgument;
import org.jd.core.v1.model.javasyntax.type.WildcardExtendsTypeArgument;
import org.jd.core.v1.model.javasyntax.type.WildcardTypeArgument;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker.MethodTypes;

import java.util.List;
import java.util.Optional;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ArrayLength;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.AssertInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.ConvertInstruction;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.GetField;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.InstanceOf;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.LookupSwitch;
import jd.core.model.instruction.bytecode.instruction.MonitorEnter;
import jd.core.model.instruction.bytecode.instruction.MonitorExit;
import jd.core.model.instruction.bytecode.instruction.MultiANewArray;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.Pop;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.model.instruction.bytecode.instruction.TableSwitch;
import jd.core.model.instruction.bytecode.instruction.TernaryOpStore;
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.attribute.ValuerefAttribute;
import jd.core.process.analyzer.instruction.bytecode.util.ByteCodeUtil;
import jd.core.util.SignatureUtil;

/*
 * Elimine les doubles casts et ajoute des casts devant les constantes
 * numeriques si necessaire.
 */
public final class CheckCastAndConvertInstructionVisitor
{
    private CheckCastAndConvertInstructionVisitor() {
        super();
    }

    private static void visit(ClassFile classFile, Method method,
            Instruction instruction)
    {
        LocalVariables localVariables = method.getLocalVariables(); 
        switch (instruction.getOpcode())
        {
        case Const.ARRAYLENGTH:
            visit(classFile, method, ((ArrayLength)instruction).getArrayref());
            break;
        case Const.AASTORE,
             ByteCodeConstants.ARRAYSTORE:
            visit(classFile, method, ((ArrayStoreInstruction)instruction).getArrayref());
            break;
        case ByteCodeConstants.ASSERT:
            {
                AssertInstruction ai = (AssertInstruction)instruction;
                visit(classFile, method, ai.getTest());
                if (ai.getMsg() != null) {
                    visit(classFile, method, ai.getMsg());
                }
            }
            break;
        case Const.ATHROW:
                Instruction valueref = ((AThrow)instruction).getValueref();
                Signature signature = method.getAttributeSignature();
                if (signature != null && signature.getSignature().indexOf('^') != -1) {
                    TypeMaker typeMaker = new TypeMaker(classFile.getLoader());
                    MethodTypes methodTypes = typeMaker.makeMethodTypes(signature.getSignature());
                    BaseType exceptionTypes = methodTypes.getExceptionTypes();
                    if (exceptionTypes != null && exceptionTypes.size() == 1) {
                        Type exceptionType = exceptionTypes.getFirst();
                        String expressionSignature = valueref.getReturnedSignature(classFile, localVariables);
                        Type expressionType = typeMaker.makeFromSignature(expressionSignature);
                        if (exceptionType.isGenericType() && !exceptionType.equals(expressionType)) {
                            String exceptionSignature = exceptionType.getDescriptor();
                            int descriptorIndex = classFile.getConstantPool().addConstantUtf8('T' + exceptionSignature + ';');
                            ValuerefAttribute valuerefAttribute = (ValuerefAttribute) instruction;
                            addOrUpdateCast(localVariables, classFile, valuerefAttribute, descriptorIndex);
                        }
                    }
                }
                visit(classFile, method, valueref);
            break;
        case ByteCodeConstants.UNARYOP:
            visit(classFile, method, ((UnaryOperatorInstruction)instruction).getValue());
            break;
        case ByteCodeConstants.BINARYOP,
             ByteCodeConstants.ASSIGNMENT:
            {
                BinaryOperatorInstruction boi =
                    (BinaryOperatorInstruction)instruction;
                visit(classFile, method, boi.getValue1());
                visit(classFile, method, boi.getValue2());
            }
            break;
        case Const.CHECKCAST:
            {
                CheckCast cc = (CheckCast)instruction;
                if (cc.getObjectref().getOpcode() == Const.CHECKCAST)
                {
                    cc.setObjectref(((CheckCast)cc.getObjectref()).getObjectref());
                }
                visit(classFile, method, cc.getObjectref());
            }
            break;
        case ByteCodeConstants.STORE,
             Const.ASTORE,
             Const.ISTORE:
                StoreInstruction si = (StoreInstruction)instruction;
                LocalVariable lv = localVariables.getLocalVariableWithIndexAndOffset(si.getIndex(), si.getOffset());
                if (lv != null) {
                    addOrUpdateCast(localVariables, classFile, si, lv.getSignatureIndex());
                }
                visit(classFile, method, si.getValueref());
            break;
        case ByteCodeConstants.DUPSTORE:
            visit(classFile, method, ((DupStore)instruction).getObjectref());
            break;
        case ByteCodeConstants.CONVERT,
             ByteCodeConstants.IMPLICITCONVERT:
            visit(classFile, method, ((ConvertInstruction)instruction).getValue());
            break;
        case ByteCodeConstants.IFCMP:
            {
                IfCmp ifCmp = (IfCmp)instruction;
                visit(classFile, method, ifCmp.getValue1());
                visit(classFile, method, ifCmp.getValue2());
            }
            break;
        case ByteCodeConstants.IF,
             ByteCodeConstants.IFXNULL:
            visit(classFile, method, ((IfInstruction)instruction).getValue());
            break;
        case ByteCodeConstants.COMPLEXIF:
            {
                List<Instruction> branchList =
                    ((ComplexConditionalBranchInstruction)instruction).getInstructions();
                for (int i=branchList.size()-1; i>=0; --i)
                {
                    visit(classFile, method, branchList.get(i));
                }
            }
            break;
        case Const.INSTANCEOF:
            visit(classFile, method, ((InstanceOf)instruction).getObjectref());
            break;
        case Const.INVOKEINTERFACE,
             Const.INVOKESPECIAL,
             Const.INVOKEVIRTUAL:
            {
                visit(classFile, method, ((InvokeNoStaticInstruction)instruction).getObjectref());
            }
            // intended fall through
        case Const.INVOKESTATIC,
             ByteCodeConstants.INVOKENEW:
            {
                List<String> parameterSignatures =
                    ((InvokeInstruction)instruction).
                        getListOfParameterSignatures(classFile.getConstantPool());

                if (parameterSignatures != null)
                {
                    List<Instruction> args =
                        ((InvokeInstruction)instruction).getArgs();
                    int i = parameterSignatures.size();
                    int j = args.size();

                    while (i-- > 0 && j-- > 0)
                    {
                        Instruction arg = args.get(j);

                        if (ByteCodeUtil.isLoadIntValue(arg.getOpcode()))
                        {
                            String argSignature = ((IConst)arg).getSignature();
                            String parameterSignature = parameterSignatures.get(i);

                            if (!parameterSignature.equals(argSignature))
                            {
                                // Types differents
                                int argBitFields =
                                        SignatureUtil.createArgOrReturnBitFields(argSignature);
                                int paramBitFields =
                                        SignatureUtil.createTypesBitField(parameterSignature);

                                if ((argBitFields|paramBitFields) == 0)
                                {
                                    // Ajout d'une instruction cast si les types
                                    // sont differents
                                    args.set(j, new ConvertInstruction(
                                        ByteCodeConstants.CONVERT,
                                        arg.getOffset()-1, arg.getLineNumber(),
                                        arg, parameterSignature));
                                }
                            } else if (SignatureUtil.isByteOrShortSignature(parameterSignature))
                            {
                                // Ajout d'une instruction cast pour les
                                // parametres numeriques de type byte ou short
                                args.set(j, new ConvertInstruction(
                                    ByteCodeConstants.CONVERT,
                                    arg.getOffset()-1, arg.getLineNumber(),
                                    arg, parameterSignature));
                            }
                            else
                            {
                                visit(classFile, method, arg);
                            }
                        }
                        else
                        {
                            visit(classFile, method, arg);
                        }
                    }
                }
            }
            break;
        case Const.LOOKUPSWITCH:
            visit(classFile, method, ((LookupSwitch)instruction).getKey());
            break;
        case Const.MONITORENTER:
            visit(classFile, method, ((MonitorEnter)instruction).getObjectref());
            break;
        case Const.MONITOREXIT:
            visit(classFile, method, ((MonitorExit)instruction).getObjectref());
            break;
        case Const.MULTIANEWARRAY:
            {
                Instruction[] dimensions = ((MultiANewArray)instruction).getDimensions();
                for (int i=dimensions.length-1; i>=0; --i)
                {
                    visit(classFile, method, dimensions[i]);
                }
            }
            break;
        case Const.NEWARRAY:
            visit(classFile, method, ((NewArray)instruction).getDimension());
            break;
        case Const.ANEWARRAY:
            visit(classFile, method, ((ANewArray)instruction).getDimension());
            break;
        case Const.POP:
            visit(classFile, method, ((Pop)instruction).getObjectref());
            break;
        case Const.PUTFIELD:
            {
                PutField putField = (PutField)instruction;
                ConstantFieldref cfr = classFile.getConstantPool().getConstantFieldref(putField.getIndex());
                ConstantNameAndType cnat = classFile.getConstantPool().getConstantNameAndType(cfr.getNameAndTypeIndex());
                Field field = classFile.getField(cnat.getNameIndex(), cnat.getSignatureIndex());
                if (field != null) {
                    Signature as = field.getAttributeSignature();
                    int fieldDescriptorIndex = Optional.ofNullable(as)
                                                       .map(Signature::getSignatureIndex)
                                                       .orElseGet(field::getDescriptorIndex);
                    addOrUpdateCast(localVariables, classFile, putField, fieldDescriptorIndex);
                }
                visit(classFile, method, putField.getObjectref());
                visit(classFile, method, putField.getValueref());
            }
            break;
        case Const.PUTSTATIC:
            visit(classFile, method, ((PutStatic)instruction).getValueref());
            break;
        case ByteCodeConstants.XRETURN:
            visit(classFile, method, ((ReturnInstruction)instruction).getValueref());
            break;
        case Const.TABLESWITCH:
            visit(classFile, method, ((TableSwitch)instruction).getKey());
            break;
        case ByteCodeConstants.TERNARYOPSTORE:
            visit(classFile, method, ((TernaryOpStore)instruction).getObjectref());
            break;
        case ByteCodeConstants.PREINC,
             ByteCodeConstants.POSTINC:
            visit(classFile, method, ((IncInstruction)instruction).getValue());
            break;
        case Const.GETFIELD:
            visit(classFile, method, ((GetField)instruction).getObjectref());
            break;
        case ByteCodeConstants.INITARRAY,
             ByteCodeConstants.NEWANDINITARRAY:
            {
                InitArrayInstruction iai = (InitArrayInstruction)instruction;
                visit(classFile, method, iai.getNewArray());
                if (iai.getValues() != null) {
                    visit(classFile, method, iai.getValues());
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
                    "Can not check cast and convert instruction in " +
                    instruction.getClass().getName() +
                    ", opcode=" + instruction.getOpcode());
        }
    }

    public static void visit(ClassFile classFile, Method method,
        List<Instruction> instructions)
    {
        for (int i=instructions.size()-1; i>=0; --i)
        {
            visit(classFile, method, instructions.get(i));
        }
    }

    public static void addOrUpdateCast(
            LocalVariables localVariables, ClassFile classFile,
            ValuerefAttribute valuerefAttribute, int descriptorIndex) {
        Instruction valueref = valuerefAttribute.getValueref();
        String expressionSignature = valueref.getReturnedSignature(classFile, localVariables);
        String signature = classFile.getConstantPool().getConstantUtf8(descriptorIndex);
        TypeMaker typeMaker = new TypeMaker(classFile.getLoader());
        Type receiverType = typeMaker.makeFromSignature(signature);
        if (valueref instanceof CheckCast) {
            CheckCast cc = (CheckCast) valueref;
            String castSignature = cc.getReturnedSignature(classFile, localVariables);
            Type castType = typeMaker.makeFromSignature(castSignature);
            if (castType.isObjectType()
                    && receiverType.isGenericType()
                    && receiverType.getDimension() == castType.getDimension()) {
                cc.setIndex(descriptorIndex);
            }
        } else if (expressionSignature != null) {
            Type expressionType = typeMaker.makeFromSignature(expressionSignature);
            if (receiverType.isGenericType()
                    && (!expressionType.isGenericType() || (valueref instanceof ALoad && !expressionType.equals(receiverType)))) {
                valuerefAttribute.setValueref(new CheckCast(
                        Const.CHECKCAST, valuerefAttribute.getOffset(),
                        valuerefAttribute.getLineNumber(), descriptorIndex, valueref));
            }
            if (receiverType.isObjectType() && expressionType != null && expressionType.isObjectType()) {
                ObjectType otLeft = (ObjectType) receiverType;
                ObjectType otRight = (ObjectType) expressionType;
                BaseTypeArgument typeArgsLeft = otLeft.getTypeArguments();
                BaseTypeArgument typeArgsRight = otRight.getTypeArguments();
                if (typeArgsLeft instanceof ObjectType && typeArgsRight instanceof WildcardTypeArgument
                 || typeArgsLeft instanceof GenericType && typeArgsRight instanceof WildcardExtendsTypeArgument) {
                    valuerefAttribute.setValueref(new CheckCast(
                            Const.CHECKCAST, valuerefAttribute.getOffset(),
                            valuerefAttribute.getLineNumber(), descriptorIndex, valueref));
                }
                if (typeArgsLeft != null && typeArgsLeft.isTypeArgumentList()
                && typeArgsRight != null && typeArgsRight.isTypeArgumentList()) {
                    for (int j = 0; j < typeArgsRight.getTypeArgumentList().size(); j++) {
                        TypeArgument typeArgumentLeft = typeArgsLeft.getTypeArgumentList().get(j);
                        TypeArgument typeArgumentRight = typeArgsRight.getTypeArgumentList().get(j);
                        if (typeArgumentLeft instanceof GenericType
                                && typeArgumentRight instanceof WildcardExtendsTypeArgument) {
                            valuerefAttribute.setValueref(new CheckCast(
                                    Const.CHECKCAST, valuerefAttribute.getOffset(),
                                    valuerefAttribute.getLineNumber(), descriptorIndex, valueref));
                            break;
                        }
                    }
                }
            }
        }
    }
}
