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
package jd.core.model.classfile;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.Annotations;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Signature;

public class Base
{
    private int accessFlags;
    private final Attribute[] attributes;

    public Base(int accessFlags, Attribute[] attributes)
    {
        this.setAccessFlags(accessFlags);
        this.attributes = attributes;
    }

    public Signature getAttributeSignature()
    {
        for (Attribute attribute : attributes) {
            if (attribute.getTag() == Const.ATTR_SIGNATURE) {
                return (Signature)attribute;
            }
        }
        return null;
    }

    public boolean containsAttributeDeprecated()
    {
        for (Attribute attribute : attributes) {
            if (attribute.getTag() == Const.ATTR_DEPRECATED) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAnnotationDeprecated(ClassFile classFile)
    {
        for (Attribute attribute : attributes)
        {
            if (attribute.getTag() == Const.ATTR_RUNTIME_INVISIBLE_ANNOTATIONS
             || attribute.getTag() == Const.ATTR_RUNTIME_VISIBLE_ANNOTATIONS) {
                AnnotationEntry[] annotations =
                        ((Annotations)attribute).getAnnotationEntries();
                if (containsAnnotationDeprecated(classFile, annotations)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean containsAnnotationDeprecated(
            ClassFile classFile, AnnotationEntry[] annotations)
    {
        int idsIndex =
                classFile.getConstantPool().getInternalDeprecatedSignatureIndex();

        for (AnnotationEntry annotationEntry : annotations) {
            if (idsIndex == annotationEntry.getTypeIndex()) {
                return true;
            }
        }
        return false;
    }

    public Attribute[] getAttributes()
    {
        return this.attributes;
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public void setAccessFlags(int accessFlags) {
        this.accessFlags = accessFlags;
    }
}
