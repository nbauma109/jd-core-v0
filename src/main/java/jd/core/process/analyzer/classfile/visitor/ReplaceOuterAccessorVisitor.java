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

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.process.analyzer.instruction.fast.visitor.AbstractReplaceInstructionVisitor;
import jd.core.util.SignatureUtil;

/*
 * Replace static call to "OuterClass access$0(InnerClass)" methods.
 */
public class ReplaceOuterAccessorVisitor extends AbstractReplaceInstructionVisitor<ClassFile>
{
    protected final ClassFile classFile;

    public ReplaceOuterAccessorVisitor(ClassFile classFile)
    {
        this.classFile = classFile;
    }

    @Override
    protected ClassFile match(Instruction parentFound, Instruction instruction) {
        return match(instruction);
    }

    @Override
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

    @Override
    protected Instruction newInstruction(Instruction i, ClassFile matchedClassFile)
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
