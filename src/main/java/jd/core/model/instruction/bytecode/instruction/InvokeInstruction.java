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

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.ExceptionUtil;
import org.jd.core.v1.util.StringConstants;

import java.util.List;

import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
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
            ConstantPool constants, LocalVariables localVariables)
    {
        if (constants == null) {
            return null;
        }

        ConstantCP cmr = constants.getConstantMethodref(this.getIndex());
        String internalClassName = constants.getConstantClassName(cmr.getClassIndex());
        ConstantNameAndType cnat = constants.getConstantNameAndType(
                cmr.getNameAndTypeIndex());

        String methodName = constants.getConstantUtf8(cnat.getNameIndex());
        String methodDescriptor = constants.getConstantUtf8(cnat.getSignatureIndex());

        String methodReturnedSignature = SignatureUtil.getMethodReturnedSignature(methodDescriptor);
        if (StringConstants.INTERNAL_OBJECT_SIGNATURE.equals(methodReturnedSignature) && internalClassName.charAt(0) != '[') {
            try {
                JavaClass javaClass = Repository.lookupClass(internalClassName);
                Method[] methods = javaClass.getMethods();
                for (Method method : methods) {
                    if (method.getName().equals(methodName) && method.getSignature().equals(methodDescriptor)) {
                        String genericSignature = method.getGenericSignature();
                        if (genericSignature != null) {
                            return SignatureUtil.getMethodReturnedSignature(genericSignature);
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                assert ExceptionUtil.printStackTrace(e);
            }
        }
        return methodReturnedSignature;
    }

    public List<String> getListOfParameterSignatures(ConstantPool constants)
    {
        if (constants == null) {
            return null;
        }

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
