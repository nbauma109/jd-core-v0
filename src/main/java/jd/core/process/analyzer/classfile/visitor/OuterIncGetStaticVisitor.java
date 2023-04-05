/*******************************************************************************
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
 ******************************************************************************/
package jd.core.process.analyzer.classfile.visitor;

import org.apache.bcel.Const;

import java.util.Map;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.accessor.Accessor;
import jd.core.model.classfile.accessor.AccessorConstants;
import jd.core.model.classfile.accessor.IncGetStaticAccessor;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;

/*
 * Replace OuterIncGetStatic.access$106();
 * par '--OuterIncGetStatic.modCount'
 */
public class OuterIncGetStaticVisitor extends OuterGetStaticVisitor
{
    public OuterIncGetStaticVisitor(
        Map<String, ClassFile> innerClassesMap, ConstantPool constants)
    {
        super(innerClassesMap, constants);
    }

    @Override
    protected byte getTargetTag()
    {
        return AccessorConstants.ACCESSOR_INC_GETSTATIC;
    }

    @Override
    protected Instruction newInstruction(Instruction i, Accessor a)
    {
        IncGetStaticAccessor igfa = (IncGetStaticAccessor)a;

        int nameIndex = this.constants.addConstantUtf8(igfa.fieldName());
        int descriptorIndex =
            this.constants.addConstantUtf8(igfa.fieldDescriptor());
        int cnatIndex =
            this.constants.addConstantNameAndType(nameIndex, descriptorIndex);

        int classNameIndex = this.constants.addConstantUtf8(igfa.className());
        int classIndex = this.constants.addConstantClass(classNameIndex);

        int cfrIndex =
             this.constants.addConstantFieldref(classIndex, cnatIndex);

        GetStatic getStatic = new GetStatic(
                Const.GETSTATIC, i.getOffset(), i.getLineNumber(),
                cfrIndex);
        return new IncInstruction(
            igfa.opcode(), i.getOffset(), i.getLineNumber(),
            getStatic, igfa.count());
    }
}
