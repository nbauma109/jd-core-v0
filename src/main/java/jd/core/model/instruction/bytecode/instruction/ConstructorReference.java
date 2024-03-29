/*******************************************************************************
 * Copyright (C) 2022 GPLv3
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

import org.jd.core.v1.model.javasyntax.type.ObjectType;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.LocalVariables;
import jd.core.printer.Printer;
import jd.core.process.writer.SourceWriteable;
import jd.core.process.writer.visitor.SourceWriterVisitor;

public class ConstructorReference extends Instruction implements SourceWriteable {

    private final ObjectType objectType;
    private final String descriptor;

    public ConstructorReference(int opcode, int offset, int lineNumber, ObjectType objectType, String descriptor) {
        super(opcode, offset, lineNumber);
        this.objectType = objectType;
        this.descriptor = descriptor;
    }

    @Override
    public String getInternalTypeName() {
        return objectType.getInternalName();
    }

    public String getDescriptor() {
        return descriptor;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    @Override
    public String getReturnedSignature(ClassFile classFile, LocalVariables localVariables) {
        return descriptor;
    }

    @Override
    public void write(Printer printer, SourceWriterVisitor visitor) {
        printer.print(objectType.getName());
        printer.print("::new");
    }
}
