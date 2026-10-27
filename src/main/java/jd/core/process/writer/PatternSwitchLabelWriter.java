/*******************************************************************************
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

import java.util.List;
import java.util.function.Consumer;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantString;
import org.jd.core.v1.api.loader.Loader;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.PatternSwitchSelector;
import jd.core.model.reference.ReferenceMap;
import jd.core.printer.Printer;
import jd.core.util.SignatureUtil;

/** Writes the label of a case of a switch on patterns. */
public final class PatternSwitchLabelWriter
{
    private static final String[] NO_CONSTANT = new String[0];

    private PatternSwitchLabelWriter() {
    }

    public static void write(
        Loader loader, Printer printer, ReferenceMap referenceMap, ClassFile classFile,
        PatternSwitchSelector selector, int key, Consumer<Instruction> guardWriter)
    {
        ConstantPool constants = classFile.getConstantPool();
        int index = selector.getLabelConstantIndex(key);

        if (index < 0)
        {
            printer.printKeyword("null");
            return;
        }

        Constant constant = constants.get(index);
        switch (constant.getTag())
        {
        case Const.CONSTANT_Class:
            {
                String internalName = constants.getConstantClassName(index);
                String signature = internalName.charAt(0) == '[' ? internalName : SignatureUtil.createTypeName(internalName);
                SignatureWriter.writeSignature(loader, printer, referenceMap, classFile, signature);
                LocalVariable binding = selector.getBindings().get(key);
                if (binding != null)
                {
                    printer.print(' ');
                    printer.print(constants.getConstantUtf8(binding.getNameIndex()));
                }
                List<LocalVariable> components = selector.getRecordComponents().get(key);
                if (components != null)
                {
                    writeComponents(loader, printer, referenceMap, classFile, components);
                }
                Instruction guard = selector.getGuards().get(key);
                if (guard != null)
                {
                    printer.print(' ');
                    printer.printKeyword("when");
                    printer.print(' ');
                    guardWriter.accept(guard);
                }
            }
            break;
        case Const.CONSTANT_String:
            {
                String value = constants.getConstantUtf8(((ConstantString) constant).getStringIndex());
                if (selector.isEnumSwitch())
                {
                    printer.print(value);
                }
                else
                {
                    ConstantValueWriter.write(loader, printer, referenceMap, classFile, constant);
                }
            }
            break;
        case Const.CONSTANT_Integer:
            printer.printNumeric(String.valueOf(((ConstantInteger) constant).getBytes()));
            break;
        case Const.CONSTANT_Dynamic:
            {
                String[] enumConstant = decodeEnumConstant(classFile, index);
                if (enumConstant.length > 0)
                {
                    SignatureWriter.writeSignature(loader, printer, referenceMap, classFile,
                        SignatureUtil.createTypeName(enumConstant[0]));
                    printer.print('.');
                    printer.print(enumConstant[1]);
                }
            }
            break;
        default:
            ConstantValueWriter.write(loader, printer, referenceMap, classFile, constant);
        }
    }

    /**
     * A qualified enum constant used as a label is the dynamic constant
     * {@code EnumDesc.of(ClassDesc.of("pkg.Type"), "NAME")}.
     *
     * @return the internal name of the enum and the name of the constant, or an empty array for any other constant
     */
    public static String[] decodeEnumConstant(ClassFile classFile, int index)
    {
        ConstantPool constants = classFile.getConstantPool();
        int[] arguments = bootstrapArguments(classFile, constants.get(index));
        if (arguments.length != 3) {
            return NO_CONSTANT;
        }
        int[] classArguments = bootstrapArguments(classFile, constants.get(arguments[1]));
        if (classArguments.length == 0
            || !(constants.get(arguments[2]) instanceof ConstantString name)
            || !(constants.get(classArguments[classArguments.length - 1]) instanceof ConstantString type)) {
            return NO_CONSTANT;
        }
        return new String[] {
            constants.getConstantUtf8(type.getStringIndex()).replace('.', '/'),
            constants.getConstantUtf8(name.getStringIndex()) };
    }

    private static int[] bootstrapArguments(ClassFile classFile, Constant constant)
    {
        var bootstrapMethods = classFile.getAttributeBootstrapMethods();
        if (!(constant instanceof org.apache.bcel.classfile.ConstantDynamic dynamic) || bootstrapMethods == null) {
            return new int[0];
        }
        return bootstrapMethods.getBootstrapMethods()[dynamic.getBootstrapMethodAttrIndex()].getBootstrapArguments();
    }

    /** Writes the components of a record pattern: {@code (int x, Object o)}. */
    public static void writeComponents(
        Loader loader, Printer printer, ReferenceMap referenceMap, ClassFile classFile,
        List<LocalVariable> components)
    {
        ConstantPool constants = classFile.getConstantPool();
        printer.print('(');
        boolean first = true;
        for (LocalVariable component : components)
        {
            if (!first)
            {
                printer.print(", ");
            }
            first = false;
            SignatureWriter.writeSignature(loader, printer, referenceMap, classFile, component.getSignature(constants));
            printer.print(' ');
            printer.print(constants.getConstantUtf8(component.getNameIndex()));
        }
        printer.print(')');
    }
}
