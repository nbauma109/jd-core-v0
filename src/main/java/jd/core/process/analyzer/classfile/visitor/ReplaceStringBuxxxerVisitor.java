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
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;
import jd.core.process.analyzer.instruction.fast.visitor.AbstractReplaceInstructionVisitor;
import jd.core.util.SignatureUtil;

public class ReplaceStringBuxxxerVisitor extends AbstractReplaceInstructionVisitor<Instruction>
{
    private final ClassFile classFile;
    private final LocalVariables localVariables;

    public ReplaceStringBuxxxerVisitor(ClassFile classFile, LocalVariables localVariables)
    {
        this.classFile = classFile;
        this.localVariables = localVariables;
    }

    @Override
    protected Instruction newInstruction(Instruction i, Instruction item) {
        return item;
    }

    @Override
    protected Instruction match(Instruction parentFound, Instruction instruction) {
        return match(instruction);
    }

    @Override
    protected Instruction match(Instruction i)
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
