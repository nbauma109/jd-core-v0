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
package jd.core.model.instruction.bytecode.instruction;

import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.Signature;

import java.util.List;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.process.deserializer.ClassFileDeserializer;
import jd.core.util.SignatureUtil;

public abstract class InvokeInstruction extends IndexInstruction
{
    private final List<Instruction> args;

    protected InvokeInstruction(
        int opcode, int offset, int lineNumber,
        int index, List<Instruction> args)
    {
        super(opcode, offset, lineNumber, index);
        this.args = args;
    }

    @Override
    public String getReturnedSignature(
            ClassFile classFile, LocalVariables localVariables)
    {
        if (classFile == null) {
            return null;
        }

        ConstantPool constants = classFile.getConstantPool();

        ConstantCP cmr = constants.getConstantMethodref(this.getIndex());
        String internalClassName = constants.getConstantClassName(cmr.getClassIndex());
        ConstantNameAndType cnat = constants.getConstantNameAndType(
                cmr.getNameAndTypeIndex());

        String methodName = constants.getConstantUtf8(cnat.getNameIndex());
        String methodDescriptor = constants.getConstantUtf8(cnat.getSignatureIndex());

        String methodReturnedSignature = SignatureUtil.getMethodReturnedSignature(methodDescriptor);
        if (classFile.getLoader().canLoad(internalClassName)) {
            ClassFile javaClass = ClassFileDeserializer.deserialize(classFile.getLoader(), internalClassName);
            Method method = javaClass.getMethod(methodName, methodDescriptor);
            if (method != null) {
                Signature genericSignature = method.getAttributeSignature();
                if (genericSignature != null) {
                    return SignatureUtil.getMethodReturnedSignature(genericSignature.getSignature());
                }
            }
        }
        return methodReturnedSignature;
    }

    public List<String> getListOfParameterSignatures(ConstantPool constants)
    {
        ConstantCP cmr = constants.getConstantMethodref(this.getIndex());
        ConstantNameAndType cnat = constants.getConstantNameAndType(
                cmr.getNameAndTypeIndex());

        String methodDescriptor =
            constants.getConstantUtf8(cnat.getSignatureIndex());


        return SignatureUtil.getParameterSignatures(methodDescriptor);
    }

    public List<Instruction> getArgs() {
        return args;
    }
}
