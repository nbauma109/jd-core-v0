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

import java.util.List;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.LocalVariables;
import jd.core.printer.Printer;
import jd.core.process.writer.SourceWriteable;
import jd.core.process.writer.visitor.SourceWriterVisitor;

public class MethodReference extends Instruction implements SourceWriteable {

    private final List<Instruction> parameters;
    private final String internalTypeName;
    private final String name;
    private final String descriptor;

    public MethodReference(int opcode, int offset, int lineNumber, List<Instruction> parameters, String internalTypeName, String name, String descriptor) {
        super(opcode, offset, lineNumber);
        this.parameters = parameters;
        this.internalTypeName = internalTypeName;
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override
    public String getReturnedSignature(ClassFile classFile, LocalVariables localVariables) {
        return descriptor;
    }

    @Override
    public String getInternalTypeName() {
        return internalTypeName;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public List<Instruction> getParameters() {
        return parameters;
    }

    @Override
    public void write(Printer printer, SourceWriterVisitor visitor) {
        if (!parameters.isEmpty()) {
            Instruction parameter = parameters.get(0);
            if (parameter instanceof ALoad aLoad) {
                visitor.visit(aLoad);
            }
            if (parameter instanceof DupLoad dupLoad) {
                Instruction receiver = dupLoad.getDupStore().getObjectref();
                if (receiver instanceof CheckCast cast) {
                    // 'receiver::name' with a cast: the cast must not apply to the whole method reference
                    if (cast.getObjectref() instanceof Invokestatic) {
                        // The type of a generic call such as 'Objects.requireNonNull(x)' is inferred
                        receiver = cast.getObjectref();
                    } else {
                        printer.print('(');
                        visitor.visit(receiver);
                        printer.print(')');
                        receiver = null;
                    }
                }
                if (receiver != null) {
                    visitor.visit(receiver);
                }
            }
        }
        printer.print("::");
        printer.print(name);
    }
}
