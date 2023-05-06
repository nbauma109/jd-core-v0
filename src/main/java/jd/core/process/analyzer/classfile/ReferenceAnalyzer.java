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
package jd.core.process.analyzer.classfile;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.AnnotationElementValue;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.Annotations;
import org.apache.bcel.classfile.ArrayElementValue;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ClassElementValue;
import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.classfile.ElementValue;
import org.apache.bcel.classfile.ElementValuePair;
import org.apache.bcel.classfile.EnumElementValue;
import org.apache.bcel.classfile.ParameterAnnotationEntry;
import org.apache.bcel.classfile.ParameterAnnotations;
import org.apache.bcel.classfile.Signature;
import org.jd.core.v1.util.StringConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.reference.Reference;
import jd.core.model.reference.ReferenceMap;
import jd.core.process.analyzer.classfile.visitor.ReferenceVisitor;

public final class ReferenceAnalyzer
{
    private ReferenceAnalyzer() {
    }

    public static void analyze(
        ReferenceMap referenceMap, ClassFile classFile)
    {
        collectReferences(referenceMap, classFile);
        reduceReferences(referenceMap);
    }

    private static void collectReferences(
            ReferenceMap referenceMap, ClassFile classFile)
    {
        // Class
        referenceMap.add(classFile.getThisClassName());

        Signature as = classFile.getAttributeSignature();
        if (as == null)
        {
            // Super class
            if (classFile.getSuperClassIndex() != 0
                && !classFile.getClassName().equals(getSimpleName(classFile.getSuperClassName()))) {
                referenceMap.add(classFile.getSuperClassName());
            }
            // Interfaces
            int[] interfaces = classFile.getInterfaces();
            String internalInterfaceName;
            for(int i=interfaces.length-1; i>=0; --i)
            {
                internalInterfaceName = classFile.getConstantPool().getConstantClassName(interfaces[i]);
                if (!classFile.getClassName().equals(getSimpleName(internalInterfaceName))) {
                    referenceMap.add(internalInterfaceName);
                }
            }
        }
        else
        {
            String signature =
                classFile.getConstantPool().getConstantUtf8(as.getSignatureIndex());
            SignatureAnalyzer.analyzeClassSignature(referenceMap, signature);
        }

        // Class annotations
        countReferencesInAttributes(
            referenceMap, classFile.getConstantPool(), classFile.getAttributes());

        // Inner classes
        List<ClassFile> innerClassFiles = classFile.getInnerClassFiles();
        if (innerClassFiles != null) {
            for (ClassFile innerClassFile : innerClassFiles) {
                collectReferences(referenceMap, innerClassFile);
            }
        }

        ReferenceVisitor visitor =
            new ReferenceVisitor(classFile.getConstantPool(), referenceMap);

        // Fields
        countReferencesInFields(referenceMap, visitor, classFile);

        // Methods
        countReferencesInMethods(referenceMap, visitor, classFile);
    }

    private static String getSimpleName(String internalName) {
        return internalName.substring(internalName.lastIndexOf(StringConstants.INTERNAL_PACKAGE_SEPARATOR) + 1); // strip the package name
    }

    private static void countReferencesInAttributes(
            ReferenceMap referenceMap, ConstantPool constants,
            Attribute[] attributes)
    {
        for (Attribute attribute : attributes) {
            if (attribute.getTag() == Const.ATTR_RUNTIME_INVISIBLE_ANNOTATIONS
             || attribute.getTag() == Const.ATTR_RUNTIME_VISIBLE_ANNOTATIONS) {
                AnnotationEntry[] annotations =
                    ((Annotations)attribute)
                    .getAnnotationEntries();
                for (AnnotationEntry annotationEntry : annotations) {
                    countAnnotationReference(referenceMap, constants, annotationEntry);
                }
            } else if (attribute.getTag() == Const.ATTR_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS
                    || attribute.getTag() == Const.ATTR_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS) {
                ParameterAnnotationEntry[] parameterAnnotations =
                    ((ParameterAnnotations)
                            attribute).getParameterAnnotationEntries();
                countParameterAnnotationsReference(referenceMap, constants, parameterAnnotations);
            }
        }
    }

    private static void countAnnotationReference(
            ReferenceMap referenceMap, ConstantPool constants,
            AnnotationEntry annotations)
    {
        String typeName = constants.getConstantUtf8(annotations.getTypeIndex());
        SignatureAnalyzer.analyzeSimpleSignature(referenceMap, typeName);

        ElementValuePair[] elementValuePairs =
            annotations.getElementValuePairs();
        for (int j=elementValuePairs.length-1; j>=0; --j) {
            countElementValue(
                referenceMap, constants, elementValuePairs[j].getValue());
        }
    }

    private static void countParameterAnnotationsReference(
            ReferenceMap referenceMap, ConstantPool constants,
            ParameterAnnotationEntry[] parameterAnnotations)
    {
        for (ParameterAnnotationEntry parameterAnnotationEntry : parameterAnnotations) {
            for (AnnotationEntry annotationEntry : parameterAnnotationEntry.getAnnotationEntries()) {
                countAnnotationReference(referenceMap, constants, annotationEntry);
            }
        }
    }

    private static void countElementValue(
            ReferenceMap referenceMap, ConstantPool constants, ElementValue ev)
    {
        if (ev instanceof ClassElementValue evci) {
            String signature = evci.getClassString();
            SignatureAnalyzer.analyzeSimpleSignature(referenceMap, signature);
        }
        if (ev instanceof AnnotationElementValue evanv) {
            countAnnotationReference(
                    referenceMap, constants, evanv.getAnnotationEntry());
        }
        if (ev instanceof ArrayElementValue evarv) {
            ElementValue[] values = evarv.getElementValuesArray();

            if (values != null)
            {
                for (ElementValue value : values) {
                    if (value instanceof ClassElementValue evci)
                    {
                        String signature = evci.getClassString();
                        SignatureAnalyzer.analyzeSimpleSignature(referenceMap, signature);
                    }
                }
            }
        }
        if (ev instanceof EnumElementValue evecv) {
            String signature = evecv.getEnumTypeString();
            SignatureAnalyzer.analyzeSimpleSignature(referenceMap, signature);
        }
    }

    private static void countReferencesInFields(
            ReferenceMap referenceMap, ReferenceVisitor visitor,
            ClassFile classFile)
    {
        String signature;
        for (Field field : classFile.getFields())
        {
            if ((field.getAccessFlags() & Const.ACC_SYNTHETIC) != 0) {
                continue;
            }

            countReferencesInAttributes(
                    referenceMap, classFile.getConstantPool(), field.getAttributes());

            signature = classFile.getConstantPool().getConstantUtf8(field.getSignatureIndex());
            SignatureAnalyzer.analyzeSimpleSignature(referenceMap, signature);

            if (field.getValueAndMethod() != null) {
                visitor.visit(field.getValueAndMethod().value());
            }
        }
    }

    private static void countReferencesInMethods(
            ReferenceMap referenceMap, ReferenceVisitor visitor,
            ClassFile classFile)
    {
        Method[] methods = classFile.getMethods();

        ConstantPool constants = classFile.getConstantPool();

        Method method;
        String signature;
        int[] exceptionIndexes;
        ElementValue defaultAnnotationValue;
        LocalVariables localVariables;
        CodeException[] codeExceptions;
        for (int i=methods.length-1; i>=0; --i)
        {
            method = methods[i];

            if ((method.getAccessFlags() &
                 (Const.ACC_SYNTHETIC|Const.ACC_BRIDGE)) != 0 ||
                method.containsError()) {
                continue;
            }

            countReferencesInAttributes(
                referenceMap, classFile.getConstantPool(), method.getAttributes());

            // Signature
            signature = constants.getConstantUtf8(method.getSignatureIndex());
            SignatureAnalyzer.analyzeMethodSignature(referenceMap, signature);

            // Exceptions
            exceptionIndexes = method.getExceptionIndexes();
            if (exceptionIndexes != null) {
                for (int j=exceptionIndexes.length-1; j>=0; --j) {
                    referenceMap.add(
                        constants.getConstantClassName(exceptionIndexes[j]));
                }
            }

            // Default annotation method value
            defaultAnnotationValue = method.getDefaultAnnotationValue();
            if (defaultAnnotationValue != null) {
                countElementValue(
                    referenceMap, constants, defaultAnnotationValue);
            }

            // Local variables
            localVariables = method.getLocalVariables();
            if (localVariables != null) {
                countReferencesInLocalVariables(
                    referenceMap, constants, localVariables);
            }

            // Code exceptions
            codeExceptions = method.getCodeExceptions();
            if (codeExceptions != null) {
                countReferencesInCodeExceptions(
                    referenceMap, constants, codeExceptions);
            }

            // Code
            countReferencesInCode(visitor, method);
        }
    }

    private static void countReferencesInLocalVariables(
            ReferenceMap referenceMap, ConstantPool constants,
            LocalVariables localVariables)
    {
        LocalVariable lv;
        for (int i=localVariables.size()-1; i>=0; --i)
        {
            lv = localVariables.getLocalVariableAt(i);

            if (lv != null && lv.getSignatureIndex() > 0)
            {
                String signature =
                    lv.getSignature(constants);
                SignatureAnalyzer.analyzeSimpleSignature(referenceMap, signature);
            }
        }
    }

    private static void countReferencesInCodeExceptions(
            ReferenceMap referenceMap, ConstantPool constants,
            CodeException[] codeExceptions)
    {
        for (CodeException ce : codeExceptions)
        {
            if (ce.getCatchType() != 0)
            {
                String internalClassName =
                    constants.getConstantClassName(ce.getCatchType());
                referenceMap.add(internalClassName);
            }
        }
    }

    private static void countReferencesInCode(
        ReferenceVisitor visitor, Method method)
    {
        List<Instruction> instructions = method.getFastNodes();

        if (instructions != null)
        {
            instructions.forEach(visitor::visit);
        }
    }

    private static void reduceReferences(ReferenceMap referenceMap)
    {
        Map<String, Boolean> multipleInternalClassName =
            new HashMap<>();

        for (Reference reference : referenceMap.values())
        {
            String internalName = reference.getInternalName();

            int index =
                internalName.lastIndexOf(StringConstants.INTERNAL_PACKAGE_SEPARATOR);
            String internalClassName =
                index != -1 ? internalName.substring(index+1) : internalName;

            multipleInternalClassName.put(internalClassName, multipleInternalClassName.containsKey(internalClassName));
        }
    }
}
