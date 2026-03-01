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

import java.util.Collections;
import java.util.List;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.attribute.ObjectrefAttribute;
import jd.core.printer.Printer;
import jd.core.process.writer.SourceWriteable;
import jd.core.process.writer.visitor.SourceWriterVisitor;

public class TypeSwitch extends Instruction implements ObjectrefAttribute, SourceWriteable {
    private Instruction objectref;
    private final List<String> caseTypes;

    public TypeSwitch(int opcode, int offset, int lineNumber, Instruction objectref, List<String> caseTypes) {
        super(opcode, offset, lineNumber);
        this.objectref = objectref;
        this.caseTypes = caseTypes == null ? Collections.emptyList() : caseTypes;
    }

    @Override
    public String getReturnedSignature(ClassFile classFile, LocalVariables localVariables) {
        return "I";
    }

    @Override
    public Instruction getObjectref() {
        return objectref;
    }

    @Override
    public void setObjectref(Instruction objectref) {
        this.objectref = objectref;
    }

    public List<String> getCaseTypes() {
        return caseTypes;
    }

    public String getCaseType(int index) {
        if (index < 0 || index >= caseTypes.size()) {
            return null;
        }
        return caseTypes.get(index);
    }

    @Override
    public void write(Printer printer, SourceWriterVisitor visitor) {
        if (objectref == null) {
            printer.printKeyword("null");
            return;
        }
        visitor.visit(objectref);
    }

    @Override
    public String getInternalTypeName() {
        return "java/lang/Object";
    }
}
