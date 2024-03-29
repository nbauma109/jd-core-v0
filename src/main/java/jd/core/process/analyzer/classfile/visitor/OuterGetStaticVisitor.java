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

import java.util.Map;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.accessor.Accessor;
import jd.core.model.classfile.accessor.AccessorConstants;
import jd.core.model.classfile.accessor.GetStaticAccessor;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.process.analyzer.instruction.fast.visitor.AbstractReplaceInstructionVisitor;

/*
 * Replace 'EntitlementFunctionLibrary.access$000()'
 * par 'EntitlementFunctionLibrary.kernelId'
 */
public class OuterGetStaticVisitor extends AbstractReplaceInstructionVisitor<Accessor>
{
    protected final Map<String, ClassFile> innerClassesMap;
    protected final ConstantPool constants;

    public OuterGetStaticVisitor(
        Map<String, ClassFile> innerClassesMap, ConstantPool constants)
    {
        this.innerClassesMap = innerClassesMap;
        this.constants = constants;
    }

    @Override
    protected Accessor match(Instruction parentFound, Instruction instruction) {
        return match(instruction);
    }

    @Override
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

    @Override
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
