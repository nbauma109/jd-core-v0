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
import jd.core.process.analyzer.instruction.bytecode.util.ByteCodeUtil;

public final class ClassFileDeserializer
{
    private ClassFileDeserializer() {
    }

    public static ClassFile deserialize(Loader loader, String internalClassPath)
    {
        return deserialize(loader, internalClassPath, false);
    }

    public static ClassFile deserialize(Loader loader, String internalClassPath, boolean skipInnerClasses)
    {
        ClassFile classFile = loadSingleClass(loader, internalClassPath);
        if (classFile == null) {
            return null;
        }

        if (classFile.getSuperClassName() != null
                && !StringConstants.JAVA_LANG_OBJECT.equals(internalClassPath)
                && loader.canLoad(classFile.getSuperClassName())) {
            ClassFile superClass = deserialize(loader, classFile.getSuperClassName(), true);
            classFile.getSuperClassAndInterfaces().put(classFile.getSuperClassName(), superClass);
        }

        for (int interfaceIndex : classFile.getInterfaces()) {
            String interfaceName = classFile.getConstantPool().getConstantClassName(interfaceIndex);
            if (loader.canLoad(interfaceName)) {
                ClassFile interfass = deserialize(loader, interfaceName);
                classFile.getSuperClassAndInterfaces().put(interfaceName, interfass);
            }
        }

        if (skipInnerClasses) {
            return classFile;
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

        List<ClassFile> innerClassFiles = new ArrayList<>(aics.getLength());

        for (InnerClass innerClass : aics.getInnerClasses())
        {
            String innerInternalClassPath =
                constants.getConstantClassName(innerClass.getInnerClassIndex());

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
                innerClassFile.setAccessFlags(innerClass.getInnerAccessFlags());
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
        if (loader.canLoad(internalClassPath))
        {
            try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(loader.load(internalClassPath))))
            {
                classFile = deserialize(dis, loader, internalClassPath);
            }
            catch (IOException e)
            {
                assert ExceptionUtil.printStackTrace(e);
            }
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
        Field[] fieldInfos = Stream.of(javaClass.getFields()).map(Field::new).toArray(Field[]::new);
        Method[] methodInfos = Stream.of(javaClass.getMethods()).map(Method::new).toArray(Method[]::new);

        for (Method method : methodInfos) {
            byte[] methodCode = method.getCode();
            if (methodCode == null) {
                continue;
            }
            ByteCodeUtil.cleanUpByteCode(methodCode);
        }

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
