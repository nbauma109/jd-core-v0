/*
 * Copyright (C) 2026 Nicolas Baumann GPLv3
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package jd.core.util;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantNameAndType;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;

/** Resolves javac's direct switch on an enum ordinal (without a switch map). */
public final class DirectEnumSwitch {
    private DirectEnumSwitch() {}

    public static String enumName(ClassFile classFile, Invokevirtual invocation) {
        if (!invocation.getArgs().isEmpty()) {
            return null;
        }
        ConstantPool pool = classFile.getConstantPool();
        ConstantCP method = pool.getConstantMethodref(invocation.getIndex());
        ConstantNameAndType nameAndType = pool.getConstantNameAndType(method.getNameAndTypeIndex());
        if (!"ordinal".equals(pool.getConstantUtf8(nameAndType.getNameIndex()))
                || !"()I".equals(pool.getConstantUtf8(nameAndType.getSignatureIndex()))) {
            return null;
        }
        String name = pool.getConstantClassName(method.getClassIndex());
        return findEnum(classFile, name) == null ? null : name;
    }

    public static String constantName(ClassFile classFile, String enumName, int ordinal) {
        ClassFile enumClass = findEnum(classFile, enumName);
        if (enumClass == null || ordinal < 0) {
            return null;
        }
        for (Field field : enumClass.getFields()) {
            if ((field.getAccessFlags() & Const.ACC_ENUM) != 0 && ordinal-- == 0) {
                return field.getName(enumClass.getConstantPool());
            }
        }
        return null;
    }

    private static ClassFile findEnum(ClassFile classFile, String name) {
        while (classFile.getOuterClass() != null) {
            classFile = classFile.getOuterClass();
        }
        return findEnumInTree(classFile, name);
    }

    private static ClassFile findEnumInTree(ClassFile classFile, String name) {
        if (name.equals(classFile.getThisClassName())) {
            return (classFile.getAccessFlags() & Const.ACC_ENUM) != 0 ? classFile : null;
        }
        if (classFile.getInnerClassFiles() != null) {
            for (ClassFile inner : classFile.getInnerClassFiles()) {
                ClassFile result = findEnumInTree(inner, name);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
