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
                writeReceiver(printer, visitor, dupLoad.getDupStore().getObjectref());
            }
        }
        printer.print("::");
        printer.print(name);
    }

    private static void writeReceiver(Printer printer, SourceWriterVisitor visitor, Instruction receiver) {
        if (!(receiver instanceof CheckCast cast)) {
            visitor.visit(receiver);
        } else if (cast.getObjectref() instanceof Invokestatic call && hasTypeOfCast(visitor, call, cast)) {
            // The type of a generic call such as Objects.requireNonNull(x) is inferred from its argument: no cast
            visitor.visit(call);
        } else {
            // The cast must not apply to the whole method reference
            printer.print('(');
            visitor.visit(receiver);
            printer.print(')');
        }
    }

    /** The argument of the call already has the type of the cast: it is the type that the compiler infers for the call. */
    private static boolean hasTypeOfCast(SourceWriterVisitor visitor, Invokestatic call, CheckCast cast) {
        String castSignature = cast.getReturnedSignature(visitor.getClassFile(), visitor.getLocalVariables());
        return call.getArgs().stream().findFirst()
            .map(argument -> castSignature.equals(
                eraseTypeArguments(argument.getReturnedSignature(visitor.getClassFile(), visitor.getLocalVariables()))))
            .orElse(false);
    }

    /** 'Ljava/util/List<Ljava/lang/String;>;' becomes 'Ljava/util/List;' */
    private static String eraseTypeArguments(String signature) {
        if (signature == null) {
            return "";
        }
        StringBuilder erased = new StringBuilder();
        int depth = 0;
        for (char c : signature.toCharArray()) {
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                depth--;
            } else if (depth == 0) {
                erased.append(c);
            }
        }
        return erased.toString();
    }
}
