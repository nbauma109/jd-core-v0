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
package jd.core.process.writer;

import org.apache.bcel.classfile.AnnotationElementValue;
import org.apache.bcel.classfile.ArrayElementValue;
import org.apache.bcel.classfile.ClassElementValue;
import org.apache.bcel.classfile.ElementValue;
import org.apache.bcel.classfile.EnumElementValue;
import org.apache.bcel.classfile.SimpleElementValue;
import org.jd.core.v1.api.loader.Loader;

import jd.core.model.classfile.ClassFile;
import jd.core.model.reference.ReferenceMap;
import jd.core.printer.Printer;
import jd.core.util.SignatureUtil;

public final class ElementValueWriter
{
    private ElementValueWriter() {
        super();
    }

    public static void writeElementValue(
        Loader loader, Printer printer, ReferenceMap referenceMap,
        ClassFile classFile, ElementValue ev)
    {
        if (ev instanceof SimpleElementValue evpt) {
            ElementValuePrimitiveTypeWriter.write(
                loader, printer, referenceMap, classFile, evpt);
        }

        if (ev instanceof ClassElementValue evci) {
            String signature = evci.getClassString();
            SignatureWriter.writeSignature(
                loader, printer, referenceMap, classFile, signature);
            printer.print('.');
            printer.printKeyword("class");
        }

        if (ev instanceof AnnotationElementValue evav) {
            AnnotationWriter.writeAnnotation(
                loader, printer, referenceMap,
                classFile, evav.getAnnotationEntry());
        }

        if (ev instanceof ArrayElementValue evarv) {
            ElementValue[] values = evarv.getElementValuesArray();
            printer.print('{');

            if (values.length > 0)
            {
                writeElementValue(
                    loader, printer, referenceMap, classFile, values[0]);
                for (int i=1; i<values.length; i++)
                {
                    printer.print(", ");
                    writeElementValue(
                        loader, printer, referenceMap, classFile, values[i]);
                }
            }
            printer.print('}');
        }

        if (ev instanceof EnumElementValue evecv) {
            String signature = evecv.getEnumTypeString();
            String constName = evecv.getEnumValueString();
            String internalClassName = SignatureUtil.getInternalName(signature);

            SignatureWriter.writeSignature(
                loader, printer, referenceMap, classFile, signature);

            printer.print('.');
            printer.printStaticField(
                internalClassName, constName,
                signature, classFile.getThisClassName());
        }
    }
}
