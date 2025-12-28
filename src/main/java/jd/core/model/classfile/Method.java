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
package jd.core.model.classfile;


import org.apache.bcel.Const;
import org.apache.bcel.classfile.AnnotationDefault;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.classfile.ElementValue;
import org.apache.bcel.classfile.ExceptionTable;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.LocalVariableTypeTable;
import org.apache.bcel.classfile.ParameterAnnotationEntry;
import org.apache.bcel.classfile.RuntimeInvisibleParameterAnnotations;
import org.apache.bcel.classfile.RuntimeVisibleParameterAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import jd.core.model.instruction.bytecode.instruction.Instruction;

public class Method extends Base implements SignatureInfo
{
    private FieldOrMethod fieldOrMethod;
    private boolean containsError;
    private int[] exceptionIndexes; // NO_UCD (use final)
    private byte[] code; // NO_UCD (use final)
    private LineNumber[] lineNumbers; // NO_UCD (use final)
    private CodeException[] codeExceptions;
    private ParameterAnnotationEntry[] visibleParameterAnnotations; // NO_UCD (use final)
    private ParameterAnnotationEntry[] invisibleParameterAnnotations; // NO_UCD (use final)
    private ElementValue defaultAnnotationValue; // NO_UCD (use final)
    private List<Instruction> instructions;
    private List<Instruction> fastNodes;
    private LocalVariables localVariables;

    /**
     * Champs permettant l'affichage des parametres des instanciations des
     * classes anonymes.
     */
    private int superConstructorParameterCount;

    public Method(FieldOrMethod fieldOrMethod)
    {
        super(fieldOrMethod.getAccessFlags(), fieldOrMethod.getAttributes());
        this.fieldOrMethod = fieldOrMethod;
        this.containsError = false;
        this.exceptionIndexes = null;
        this.code = null;
        this.localVariables = null;
        this.lineNumbers = null;
        this.codeExceptions = null;
        this.visibleParameterAnnotations = null;
        this.invisibleParameterAnnotations = null;
        this.defaultAnnotationValue = null;
        this.superConstructorParameterCount = 0;

        Code ac = null;

        for (Attribute attribute : fieldOrMethod.getAttributes())
        {
            switch (attribute.getTag())
            {
            case Const.ATTR_EXCEPTIONS:
                this.exceptionIndexes =
                    ((ExceptionTable)attribute).getExceptionIndexTable();
                break;
            case Const.ATTR_CODE:
                ac = (Code)attribute;
                break;
            case Const.ATTR_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:
                this.visibleParameterAnnotations =
                    ((RuntimeVisibleParameterAnnotations)attribute).getParameterAnnotationEntries();
                break;
            case Const.ATTR_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:
                this.invisibleParameterAnnotations =
                    ((RuntimeInvisibleParameterAnnotations)attribute).getParameterAnnotationEntries();
                break;
            case Const.ATTR_ANNOTATION_DEFAULT:
                this.defaultAnnotationValue =
                    ((AnnotationDefault)attribute).getDefaultValue();
                break;
            }
        }

        if (ac != null)
        {
            this.code = ac.getCode();

            // localVariables
            LocalVariableTable alvt = ac.getLocalVariableTable();
            if (alvt != null && alvt.getLocalVariableTable().length > 0)
            {
                LocalVariableTypeTable alvtt = (LocalVariableTypeTable) Stream.of(ac.getAttributes())
                        .filter(LocalVariableTypeTable.class::isInstance).findAny().orElse(null);
                LocalVariable[] localVariableTypeTable = Optional.ofNullable(alvtt)
                        .map(LocalVariableTypeTable::getLocalVariableTypeTable).orElse(null);
                this.localVariables = new LocalVariables(
                    Stream.of(alvt.getLocalVariableTable())
                        .map(jd.core.model.classfile.LocalVariable::new)
                        .toArray(jd.core.model.classfile.LocalVariable[]::new),
                    Optional.ofNullable(localVariableTypeTable).map(Stream::of).orElseGet(Stream::empty)
                        .map(jd.core.model.classfile.LocalVariable::new)
                        .toArray(jd.core.model.classfile.LocalVariable[]::new));
            }

            // lineNumbers
            LineNumberTable ant = ac.getLineNumberTable();
            this.lineNumbers = Optional.ofNullable(ant).map(LineNumberTable::getLineNumberTable).orElse(null);

            // codeExceptions
            this.codeExceptions = ac.getExceptionTable();
        }
    }

    public boolean containsError()
    {
        return containsError;
    }

    public void setContainsError(boolean containsError)
    {
        this.containsError = containsError;
    }

    public int[] getExceptionIndexes()
    {
        return this.exceptionIndexes;
    }

    public LocalVariables getLocalVariables()
    {
        return this.localVariables;
    }

    public void setLocalVariables(LocalVariables llv)
    {
        this.localVariables = llv;
    }

    public List<Instruction> getInstructions()
    {
        return instructions;
    }
    public void setInstructions(List<Instruction> instructions)
    {
        this.instructions = instructions;
    }

    public List<Instruction> getFastNodes()
    {
        return fastNodes;
    }

    public void setFastNodes(List<Instruction> fastNodes)
    {
        this.fastNodes = fastNodes;
    }

    public byte[] getCode()
    {
        return this.code;
    }

    public LineNumber[] getLineNumbers()
    {
        return lineNumbers;
    }

    public CodeException[] getCodeExceptions()
    {
        return this.codeExceptions;
    }

    public ParameterAnnotationEntry[] getVisibleParameterAnnotations()
    {
        return this.visibleParameterAnnotations;
    }

    public ParameterAnnotationEntry[] getInvisibleParameterAnnotations()
    {
        return this.invisibleParameterAnnotations;
    }

    public ElementValue getDefaultAnnotationValue()
    {
        return this.defaultAnnotationValue;
    }

    public int getSuperConstructorParameterCount()
    {
        return superConstructorParameterCount;
    }

    public void setSuperConstructorParameterCount(int count)
    {
        this.superConstructorParameterCount = count;
    }

    public int getNameIndex() {
        return fieldOrMethod.getNameIndex();
    }

    public String getName(ConstantPool constants) {
        return constants.getConstantUtf8(fieldOrMethod.getNameIndex());
    }

    public String getDescriptor(ConstantPool constants) {
        return constants.getConstantUtf8(fieldOrMethod.getSignatureIndex());
    }

    @Override
    public int getDescriptorIndex() {
        return fieldOrMethod.getSignatureIndex();
    }

    public boolean isOverride(ClassFile classFile) {
        ConstantPool cp = classFile.getConstantPool();
        if (getNameIndex() != cp.getInstanceConstructorIndex()
         && getNameIndex() != cp.getClassConstructorIndex()
         && (getAccessFlags() & (Const.ACC_PRIVATE|Const.ACC_ABSTRACT|Const.ACC_STATIC)) == 0) {
            String name = getName(cp);
            String descriptor = getDescriptor(cp);
            Method overridenMethod = classFile.findMethodInSuperClassAndInterfaces(name, descriptor);
            if (overridenMethod != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isLambda(ConstantPool constants) {
        return getName(constants).startsWith("lambda$");
    }

    public org.apache.bcel.classfile.Method getMethod() {
        return (org.apache.bcel.classfile.Method) fieldOrMethod;
    }

}
