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
package jd.core.process.deserializer;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.InnerClass;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.JavaClass;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.ExceptionUtil;
import org.jd.core.v1.util.StringConstants;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.Method;

public final class ClassFileDeserializer
{
    private ClassFileDeserializer() {
        super();
    }

    public static ClassFile deserialize(Loader loader, String internalClassPath)
    {
        ClassFile classFile = loadSingleClass(loader, internalClassPath);
        if (classFile == null) {
            return null;
        }

        InnerClasses aics = classFile.getAttributeInnerClasses();
        if (aics == null) {
            return classFile;
        }

        String internalClassPathPrefix =
            internalClassPath.substring(
                0, internalClassPath.length() - StringConstants.CLASS_FILE_SUFFIX.length());
        String innerInternalClassNamePrefix =
            internalClassPathPrefix + StringConstants.INTERNAL_INNER_SEPARATOR;
        ConstantPool constants = classFile.getConstantPool();

        InnerClass[] cs = aics.getInnerClasses();
        int length = cs.length;
        List<ClassFile> innerClassFiles = new ArrayList<>(length);

        for (int i=0; i<length; i++)
        {
            String innerInternalClassPath =
                constants.getConstantClassName(cs[i].getInnerClassIndex());

            if (! innerInternalClassPath.startsWith(innerInternalClassNamePrefix)) {
                continue;
            }
            int offsetInternalInnerSeparator = innerInternalClassPath.indexOf(
                StringConstants.INTERNAL_INNER_SEPARATOR,
                innerInternalClassNamePrefix.length());
            if (offsetInternalInnerSeparator != -1)
            {
                String tmpInnerInternalClassPath =
                    innerInternalClassPath.substring(0, offsetInternalInnerSeparator) +
                    StringConstants.CLASS_FILE_SUFFIX;
                if (loader.canLoad(tmpInnerInternalClassPath)) {
                    // 'innerInternalClassName' is not a direct inner class.
                    continue;
                }
            }

            ClassFile innerClassFile =
                deserialize(loader, innerInternalClassPath +
                StringConstants.CLASS_FILE_SUFFIX);

            if (innerClassFile != null)
            {
                // Alter inner class access flag
                innerClassFile.setAccessFlags(cs[i].getInnerAccessFlags());
                // Setup outer class reference
                innerClassFile.setOuterClass(classFile);
                // Add inner classes
                innerClassFiles.add(innerClassFile);
            }
        }

        // Add inner classes
        classFile.setInnerClassFiles(innerClassFiles);

        return classFile;
    }

    private static ClassFile loadSingleClass(
            Loader loader, String internalClassPath)
    {
        ClassFile classFile = null;
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(loader.load(internalClassPath))))
        {
            classFile = deserialize(dis, loader, internalClassPath);
        }
        catch (IOException e)
        {
            assert ExceptionUtil.printStackTrace(e);
        }
        return classFile;
    }

    private static ClassFile deserialize(DataInputStream di, Loader loader, String internalClassPath)
        throws IOException
    {
        ClassParser parser = new ClassParser(di, internalClassPath);
        JavaClass javaClass = parser.parse();

        ConstantPool constantPool = new ConstantPool(javaClass.getConstantPool());

        int accessFlags = javaClass.getAccessFlags();
        int thisClass = javaClass.getClassNameIndex();
        int superClass = javaClass.getSuperclassNameIndex();

        int[] interfaces = javaClass.getInterfaceIndices();
        Field[] fieldInfos = javaClass.getFields() == null ? null :
            Stream.of(javaClass.getFields()).map(Field::new).toArray(Field[]::new);
        Method[] methodInfos = javaClass.getMethods() == null ? null : 
            Stream.of(javaClass.getMethods()).map(m -> new Method(m, constantPool)).toArray(Method[]::new);

        return new ClassFile(
                javaClass.getMinor(),
                javaClass.getMajor(),
                constantPool,
                accessFlags, thisClass, superClass,
                interfaces,
                fieldInfos,
                methodInfos,
                javaClass.getAttributes(),
                loader
        );
    }
}
