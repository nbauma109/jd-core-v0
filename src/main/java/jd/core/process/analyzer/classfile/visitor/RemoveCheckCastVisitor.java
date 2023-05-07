/**
 * Copyright (C) 2023 GPLv3
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
 */
package jd.core.process.analyzer.classfile.visitor;

import org.jd.core.v1.model.javasyntax.type.Type;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;

public class RemoveCheckCastVisitor
{
    private final ClassFile classFile;
    private final LocalVariables localVariables;
    private final TypeMaker typeMaker;
    private final Type returnedType;

    public RemoveCheckCastVisitor(
            ClassFile classFile, LocalVariables localVariables,
            TypeMaker typeMaker, Type returnedType)
    {
        this.classFile = classFile;
        this.localVariables = localVariables;
        this.typeMaker = typeMaker;
        this.returnedType = returnedType;
    }

    public void visit(Instruction instruction)
    {
        if (instruction instanceof TernaryOperator fto) {
            if (fto.getValue1() instanceof CheckCast cc) {
                String expSignature = cc.getObjectref().getReturnedSignature(classFile, localVariables);
                if (expSignature != null) {
                    Type expType = typeMaker.makeFromSignature(expSignature);
                    if (returnedType.equals(expType)) {
                        fto.setValue1(cc.getObjectref());
                    }
                }
            } else {
                visit(fto.getValue1());
            }
            if (fto.getValue2() instanceof CheckCast cc) {
                String expSignature = cc.getObjectref().getReturnedSignature(classFile, localVariables);
                if (expSignature != null) {
                    Type expType = typeMaker.makeFromSignature(expSignature);
                    if (returnedType.equals(expType)) {
                        fto.setValue2(cc.getObjectref());
                    }
                }
            } else {
                visit(fto.getValue2());
            }
        }
    }
}
