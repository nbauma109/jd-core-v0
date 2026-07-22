/*******************************************************************************
 * Copyright (C) 2007-2023 GPLv3
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
package jd.core.process.analyzer.classfile.reconstructor;

import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantNameAndType;

import java.util.List;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.instruction.bytecode.instruction.DupLoad;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;
import jd.core.model.instruction.bytecode.instruction.Pop;
import jd.core.process.analyzer.classfile.visitor.SearchInstructionByTypeVisitor;

/*
 * Reconstruction of pattern
 * a.new A(...)
 */
public final class DotNewReconstructor
{
    private DotNewReconstructor() {
    }

    public static void reconstruct(ClassFile classFile, List<Instruction> list)
    {
        SearchInstructionByTypeVisitor<InvokeNew> visitor = new SearchInstructionByTypeVisitor<>(InvokeNew.class);
        for (int i = list.size() - 1; i >= 0; i--) {
            Instruction instruction = list.get(i);
            if (instruction instanceof Pop pop) {
                Instruction objectref = pop.getObjectref();
                if (objectref instanceof Invokevirtual || objectref instanceof Invokestatic) {
                    ConstantPool cp = classFile.getConstantPool();
                    int methodIndex = objectref instanceof Invokevirtual iv
                            ? iv.getIndex() : ((Invokestatic)objectref).getIndex();
                    ConstantCP cmr = cp.getConstantMethodref(methodIndex);
                    ConstantNameAndType cnat = cp.getConstantNameAndType(cmr.getNameAndTypeIndex());
                    String owner = cp.getConstantClassName(cmr.getClassIndex());
                    String methodName = cp.getConstantUtf8(cnat.getNameIndex());
                    String methodDesc = cp.getConstantUtf8(cnat.getSignatureIndex());
                    boolean nullCheck = "getClass".equals(methodName)
                            && "()Ljava/lang/Class;".equals(methodDesc)
                            || "java/util/Objects".equals(owner)
                            && "requireNonNull".equals(methodName)
                            && "(Ljava/lang/Object;)Ljava/lang/Object;".equals(methodDesc);
                    if (nullCheck) {
                        InvokeNew invokeNew = visitor.visit(list.get(i+1));
                        if (invokeNew != null
                            && !invokeNew.getArgs().isEmpty()
                            && invokeNew.getArgs().get(0) instanceof DupLoad dupLoad
                            && i > 0
                            && list.get(i-1) == dupLoad.getDupStore()) {
                            // Remove POP
                            list.remove(i);
                            // Remove DUPLOAD from args
                            invokeNew.getArgs().remove(0);
                            invokeNew.setPrefix(dupLoad.getDupStore().getObjectref());
                        }
                    }
                }
            }
        }
    }
}
