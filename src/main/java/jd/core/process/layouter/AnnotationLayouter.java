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
package jd.core.process.layouter;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.Annotations;
import org.apache.bcel.classfile.Attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.layout.block.AnnotationsLayoutBlock;
import jd.core.model.layout.block.LayoutBlock;

public final class AnnotationLayouter
{
    private AnnotationLayouter() {
    }

    public static void createBlocksForAnnotations(
            ClassFile classFile, Attribute[] attributes,
            List<LayoutBlock> layoutBlockList, boolean addOverride)
    {
        int attributesLength = attributes.length;
        List<AnnotationEntry> annotations =
                new ArrayList<>(attributesLength);

        for (Attribute attribute : attributes)
        {
            if (attribute.getTag() == Const.ATTR_RUNTIME_INVISIBLE_ANNOTATIONS || attribute.getTag() == Const.ATTR_RUNTIME_VISIBLE_ANNOTATIONS) {
                AnnotationEntry[] array =
                        ((Annotations)attribute).getAnnotationEntries();

                if (array != null)
                {
                    Collections.addAll(annotations, array);
                }
            }
        }

        if (addOverride) {
            ConstantPool constants = classFile.getConstantPool();
            int typeIndex = constants.getOverrideSignatureIndex();
            AnnotationEntry annotationEntry = new AnnotationEntry(typeIndex, constants.getConstantPool(), false);
            annotations.add(annotationEntry);
        }

        if (!annotations.isEmpty())
        {
            layoutBlockList.add(new AnnotationsLayoutBlock(
                    classFile, annotations));
        }
    }

    public static void createBlocksForAnnotations(
            ClassFile classFile, Attribute[] attributes,
            List<LayoutBlock> layoutBlockList)
    {
        createBlocksForAnnotations(classFile, attributes, layoutBlockList, false);
    }
}
