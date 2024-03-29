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
package jd.core.process.analyzer.instruction.bytecode.factory;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantNameAndType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokespecial;
import jd.core.util.SignatureUtil;

public class InvokespecialFactory implements InstructionFactory
{
    @Override
    public int create(
            ClassFile classFile, Method method, List<Instruction> list,
            List<Instruction> listForAnalyze,
            Deque<Instruction> stack, byte[] code, int offset,
            int lineNumber, boolean[] jumps)
    {
        final int opcode = code[offset] & 255;
        final int index = (code[offset+1] & 255) << 8 | code[offset+2] & 255;

        ConstantPool constants = classFile.getConstantPool();
        ConstantCP cmr =
            constants.getConstantMethodref(index);
        
        Objects.requireNonNull(cmr, "Invalid ConstantMethodref index");

        ConstantNameAndType cnat = constants.getConstantNameAndType(
                cmr.getNameAndTypeIndex());

        String methodDescriptor =
            constants.getConstantUtf8(cnat.getSignatureIndex());

        int nbrOfParameters = SignatureUtil.getParameterSignatureCount(methodDescriptor);
        List<Instruction> args = new ArrayList<>(nbrOfParameters);

        for (int i=nbrOfParameters; i>0; --i) {
            args.add(stack.pop());
        }

        Collections.reverse(args);

        Instruction objectref = stack.pop();

        final Instruction instruction = new Invokespecial(
            opcode, offset, lineNumber, index, objectref, args);

        if (!"V".equals(SignatureUtil.getMethodReturnedSignature(methodDescriptor))) {
            stack.push(instruction);
        } else {
            list.add(instruction);
        }

        listForAnalyze.add(instruction);

        return Const.getNoOfOperands(opcode);
    }
}
