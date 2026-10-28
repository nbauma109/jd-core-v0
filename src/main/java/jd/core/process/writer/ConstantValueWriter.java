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

import org.apache.bcel.Const;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.util.StringConstants;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.reference.ReferenceMap;
import jd.core.printer.Printer;
import jd.core.util.StringUtil;

public final class ConstantValueWriter
{
    private ConstantValueWriter() {
    }

    public static void write(
        Loader loader, Printer printer, ReferenceMap referenceMap,
        ClassFile classFile, Constant cv)
    {
        write(loader, printer, referenceMap, classFile, cv, (byte)0);
    }

    public static void write(
        Loader loader, Printer printer, ReferenceMap referenceMap,
        ClassFile classFile, Constant cv, int constantIntegerType)
    {
        ConstantPool constants = classFile.getConstantPool();

        switch (cv.getTag())
        {
          case Const.CONSTANT_Double:
            {
                double d = ((ConstantDouble)cv).getBytes();

                if (Double.compare(d, Double.POSITIVE_INFINITY) == 0)
                {
                    write(
                        loader, printer, referenceMap, classFile,
                        StringConstants.JAVA_LANG_DOUBLE, "POSITIVE_INFINITY", "D");
                }
                else if (Double.compare(d, Double.NEGATIVE_INFINITY) == 0)
                {
                    write(
                        loader, printer, referenceMap, classFile,
                        StringConstants.JAVA_LANG_DOUBLE, "NEGATIVE_INFINITY", "D");
                }
                else if (Double.isNaN(d))
                {
                    write(
                        loader, printer, referenceMap, classFile,
                        StringConstants.JAVA_LANG_DOUBLE, "NaN", "D");
                }
                else if (Double.compare(d, Double.MAX_VALUE) == 0)
                {
                    write(
                        loader, printer, referenceMap, classFile,
                        StringConstants.JAVA_LANG_DOUBLE, StringConstants.MAX_VALUE, "D");
                }
                else if (Double.compare(d, Double.MIN_VALUE) == 0)
                {
                    write(
                        loader, printer, referenceMap, classFile,
                        StringConstants.JAVA_LANG_DOUBLE, StringConstants.MIN_VALUE, "D");
                }
                else
                {
                    float widened = (float) d;
                    String value = String.valueOf(d);
                    if (widened == d && !String.valueOf(widened).equals(value)) {
                        // The compiler widened a float constant: print it as a float,
                        // with a cast so that the expression stays of type double
                        printer.print("(double) ");
                        printer.printNumeric(String.valueOf(widened) + 'F');
                        break;
                    }
                    if (value.indexOf('.') == -1) {
                        value += ".0";
                    }
                    printer.printNumeric(value + 'D');
                }
            }
            break;
          case Const.CONSTANT_Float:
            {
                float value = ((ConstantFloat)cv).getBytes();

                if (Float.compare(value, Float.POSITIVE_INFINITY) == 0)
                {
                    write(
                        loader, printer, referenceMap, classFile,
                        StringConstants.JAVA_LANG_FLOAT, "POSITIVE_INFINITY", "F");
                }
                else if (Float.compare(value, Float.NEGATIVE_INFINITY) == 0)
                {
                    write(
                        loader, printer, referenceMap, classFile,
                        StringConstants.JAVA_LANG_FLOAT, "NEGATIVE_INFINITY", "F");
                }
                else if (Float.isNaN(value))
                {
                    write(
                        loader, printer, referenceMap, classFile,
                        StringConstants.JAVA_LANG_FLOAT, "NaN", "F");
                }
                else if (Float.compare(value, Float.MAX_VALUE) == 0)
                {
                    write(
                        loader, printer, referenceMap, classFile,
                        StringConstants.JAVA_LANG_FLOAT, StringConstants.MAX_VALUE, "F");
                }
                /* else if (Float.compare(value, Float.MIN_NORMAL) == 0)
                {
                    Write(
                        loader, printer, referenceMap, classFile,
                        StringConstants.JAVA_LANG_FLOAT, "MIN_NORMAL", "F");
                } */
                else if (Float.compare(value, Float.MIN_VALUE) == 0)
                {
                    write(
                        loader, printer, referenceMap, classFile,
                        StringConstants.JAVA_LANG_FLOAT, StringConstants.MIN_VALUE, "F");
                }
                else
                {
                    // TODO Conversion de la valeur en constante ?
                    String s = String.valueOf(value);
                    if (s.indexOf('.') == -1) {
                        s += ".0";
                    }
                    printer.printNumeric(s + 'F');
                }
            }
            break;
          case Const.CONSTANT_Integer:
            {
                int value = ((ConstantInteger)cv).getBytes();

                switch (constantIntegerType)
                {
                case 'Z':
                    {
                        printer.printKeyword(value == 0 ? "false" : "true");
                    }
                    break;
                case 'C':
                    {
                        String escapedString = StringUtil.escapeCharAndAppendApostrophe((char)value);
                        String scopeInternalName = classFile.getThisClassName();
                        printer.printString(escapedString, scopeInternalName);
                    }
                    break;
                default:
                    {
                        if (value == Integer.MIN_VALUE)
                        {
                            write(
                                loader, printer, referenceMap, classFile,
                                StringConstants.JAVA_LANG_INTEGER, StringConstants.MIN_VALUE, "I");
                        }
                        else if (value == Integer.MAX_VALUE)
                        {
                            write(
                                loader, printer, referenceMap, classFile,
                                StringConstants.JAVA_LANG_INTEGER, StringConstants.MAX_VALUE, "I");
                        }
                        else
                        {
                            printer.printNumeric(String.valueOf(value));
                        }
                    }
                }
            }
            break;
          case Const.CONSTANT_Long:
            {
                long value = ((ConstantLong)cv).getBytes();

                if (value == Long.MIN_VALUE)
                {
                    write(
                        loader, printer, referenceMap, classFile,
                        StringConstants.JAVA_LANG_LONG, StringConstants.MIN_VALUE, "J");
                }
                else if (value == Long.MAX_VALUE)
                {
                    write(
                        loader, printer, referenceMap, classFile,
                        StringConstants.JAVA_LANG_LONG, StringConstants.MAX_VALUE, "J");
                }
                else
                {
                    printer.printNumeric(String.valueOf(value) + 'L');
                }
            }
            break;
          case Const.CONSTANT_String:
            {
                String s = constants.getConstantUtf8(
                    ((ConstantString)cv).getStringIndex());
                if (classFile.getMajorVersion() >= Const.MAJOR_15 && isTextBlock(s))
                {
                    writeTextBlock(printer, s, classFile.getThisClassName());
                    break;
                }
                String escapedString =
                    StringUtil.escapeStringAndAppendQuotationMark(s);
                String scopeInternalName = classFile.getThisClassName();
                printer.printString(escapedString, scopeInternalName);
            }
            break;
          case Const.CONSTANT_Utf8:
            {
                String s = ((ConstantUtf8) cv).getBytes();
                String escapedString =
                    StringUtil.escapeStringAndAppendQuotationMark(s);
                String scopeInternalName = classFile.getThisClassName();
                printer.printString(escapedString, scopeInternalName);
            }
            break;
        }
    }

    private static boolean isTextBlock(String s)
    {
        int lines = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') {
                lines++;
            }
        }
        return lines >= 3;
    }

    /** Writes a string of several lines as a text block, the closing delimiter on its own line when the text ends with a line feed. */
    private static void writeTextBlock(Printer printer, String s, String scopeInternalName)
    {
        printer.printString("\"\"\"", scopeInternalName);
        String[] lines = s.split("\n", -1);
        boolean endsWithLineFeed = s.endsWith("\n");
        int count = endsWithLineFeed ? lines.length - 1 : lines.length;
        for (int i = 0; i < count; i++)
        {
            printer.endOfLine();
            printer.startOfLine(Printer.UNKNOWN_LINE_NUMBER);
            String line = escapeTextBlockLine(lines[i], !endsWithLineFeed && i == count - 1);
            printer.printString(line, scopeInternalName);
        }
        if (endsWithLineFeed)
        {
            printer.endOfLine();
            printer.startOfLine(Printer.UNKNOWN_LINE_NUMBER);
            printer.printString("\"\"\"", scopeInternalName);
        }
        else
        {
            printer.printString("\"\"\"", scopeInternalName);
        }
    }

    private static String escapeTextBlockLine(String line, boolean last)
    {
        StringBuilder sb = new StringBuilder(line.length() + 4);
        for (int i = 0; i < line.length(); i++)
        {
            char c = line.charAt(i);
            if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '"' && line.startsWith("\"\"", i + 1)) {
                sb.append("\\\"");
            } else if (c == '\t') {
                sb.append("\\t");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\b' || c == '\f' || c < ' ') {
                sb.append(String.format("\\u%04x", (int) c));
            } else if (c == ' ' && i == line.length() - 1) {
                sb.append("\\s");
            } else {
                sb.append(c);
            }
        }
        if (last && line.endsWith("\"")) {
            // The closing delimiter follows: a quote at the end would merge with it
            sb.setLength(sb.length() - 1);
            sb.append("\\\"");
        }
        return sb.toString();
    }

    private static void write(
        Loader loader, Printer printer, ReferenceMap referenceMap,
        ClassFile classFile, String internalTypeName,
        String name, String descriptor)
    {
        String className = SignatureWriter.internalClassNameToClassName(
            loader, referenceMap, classFile, internalTypeName);
        String scopeInternalName = classFile.getThisClassName();
        printer.printType(internalTypeName, className, scopeInternalName);
        printer.print('.');
        printer.printStaticField(internalTypeName, name, descriptor, scopeInternalName);
    }

    public static void writeHexa(
        Loader loader, Printer printer, ReferenceMap referenceMap,
        ClassFile classFile, Constant cv)
    {
        switch (cv.getTag())
        {
        case Const.CONSTANT_Integer:
            printer.printNumeric(
                "0x" + Integer.toHexString( ((ConstantInteger)cv).getBytes() ).toUpperCase());
            break;
        case Const.CONSTANT_Long:
            printer.printNumeric(
                "0x" + Long.toHexString( ((ConstantLong)cv).getBytes() ).toUpperCase() + "L");
            break;
        default:
            write(loader, printer, referenceMap, classFile, cv, (byte)0);
        }
    }
}
