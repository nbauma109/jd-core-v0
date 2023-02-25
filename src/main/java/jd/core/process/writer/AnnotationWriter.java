/**
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
 */
package jd.core.process.writer;

import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ElementValuePair;
import org.apache.bcel.classfile.ParameterAnnotationEntry;
import org.jd.core.v1.api.loader.Loader;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.reference.ReferenceMap;
import jd.core.printer.Printer;

public final class AnnotationWriter
{
    private AnnotationWriter() {
    }
        public static void writeParameterAnnotation(
        Loader loader, Printer printer, ReferenceMap referenceMap,
        ClassFile classFile, ParameterAnnotationEntry parameterAnnotation)
    {
        if (parameterAnnotation == null) {
            return;
        }

        AnnotationEntry[] annotations = parameterAnnotation.getAnnotationEntries();

        if (annotations == null) {
            return;
        }

        for (AnnotationEntry annotation : annotations)
        {
            writeAnnotation(
                loader, printer, referenceMap, classFile, annotation);
            printer.print(' ');
        }
    }

    public static void writeAnnotation(
        Loader loader, Printer printer, ReferenceMap referenceMap,
        ClassFile classFile, AnnotationEntry annotationEntry)
    {
        printer.startOfAnnotationName();
        printer.print('@');
        String annotationName =
            classFile.getConstantPool().getConstantUtf8(annotationEntry.getTypeIndex());
        SignatureWriter.writeSignature(
            loader, printer, referenceMap, classFile, annotationName);
        printer.endOfAnnotationName();

        ElementValuePair[] evps = annotationEntry.getElementValuePairs();
        if (evps != null && evps.length > 0)
        {
            printer.print('(');

            ConstantPool constants = classFile.getConstantPool();
            String name = constants.getConstantUtf8(evps[0].getNameIndex());

            if (evps.length > 1 || !"value".equals(name))
            {
                printer.print(name);
                printer.print('=');
            }
            ElementValueWriter.writeElementValue(
                loader, printer, referenceMap,
                classFile, evps[0].getValue());

            for (int j=1; j<evps.length; j++)
            {
                name = constants.getConstantUtf8(evps[j].getNameIndex());

                printer.print(", ");
                printer.print(name);
                printer.print('=');
                ElementValueWriter.writeElementValue(
                    loader, printer, referenceMap,
                    classFile, evps[j].getValue());
            }

            printer.print(')');
        }
    }
}
