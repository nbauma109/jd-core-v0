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

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantInterfaceMethodref;
import org.apache.bcel.classfile.ConstantInvokeDynamic;
import org.apache.bcel.classfile.ConstantMethodHandle;
import org.apache.bcel.classfile.ConstantMethodType;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantUtf8;
import org.jd.core.v1.util.StringConstants;

import java.util.ArrayList;
import java.util.List;
import jd.core.util.IndexToIndexMap;
import jd.core.util.StringToIndexMap;

public class ConstantPool
{
    private final List<Constant> listOfConstants;
    private final StringToIndexMap constantUtf8ToIndex;
    private final IndexToIndexMap constantClassToIndex;

    private final int instanceConstructorIndex;
    private final int classConstructorIndex;
    private final int internalDeprecatedSignatureIndex;
    private final int toStringIndex;
    private final int valueOfIndex;
    private final int appendIndex;

    private final int objectClassNameIndex;
    private final int stringClassNameIndex;
    private final int stringBufferClassNameIndex;
    private final int stringBuilderClassNameIndex;

    private final int objectSignatureIndex;

    private final int thisLocalVariableNameIndex;

    public ConstantPool(org.apache.bcel.classfile.ConstantPool constantPool)
    {
        this.listOfConstants = new ArrayList<>();
        this.constantUtf8ToIndex = new StringToIndexMap();
        this.constantClassToIndex = new IndexToIndexMap();

        Constant[] constants = constantPool.getConstantPool();

        for (int i=0; i<constants.length; i++)
        {
            Constant constant = constants[i];

            int index = this.listOfConstants.size();
            this.listOfConstants.add(constant);

            if (constant instanceof ConstantUtf8)
            {
                // to convert to jdk16 pattern matching only when spotbugs #1617 and eclipse #577987 are solved
                ConstantUtf8 cu = (ConstantUtf8) constant;
                this.constantUtf8ToIndex.put(cu.getBytes(), index);
            }
            if (constant instanceof ConstantClass)
            {
                // to convert to jdk16 pattern matching only when spotbugs #1617 and eclipse #577987 are solved
                ConstantClass cc = (ConstantClass) constant;
                this.constantClassToIndex.put(cc.getNameIndex(), index);
            }
        }

        // Add instance constructor
        this.instanceConstructorIndex =
            addConstantUtf8(StringConstants.INSTANCE_CONSTRUCTOR);

        // Add class constructor
        this.classConstructorIndex =
            addConstantUtf8(StringConstants.CLASS_CONSTRUCTOR);

        // Add internal deprecated signature
        this.internalDeprecatedSignatureIndex =
            addConstantUtf8(StringConstants.INTERNAL_DEPRECATED_SIGNATURE);

        // -- Add method names --------------------------------------------- //
        // Add 'toString'
        this.toStringIndex = addConstantUtf8(StringConstants.TOSTRING_METHOD_NAME);

        // Add 'valueOf'
        this.valueOfIndex = addConstantUtf8(StringConstants.VALUEOF_METHOD_NAME);

        // Add 'append'
        this.appendIndex = addConstantUtf8(StringConstants.APPEND_METHOD_NAME);

        // -- Add class names ---------------------------------------------- //
        // Add 'Object'
        this.objectClassNameIndex =
            addConstantUtf8(StringConstants.JAVA_LANG_OBJECT);
        this.objectSignatureIndex =
            addConstantUtf8(StringConstants.INTERNAL_OBJECT_SIGNATURE);

        // Add 'String'
        this.stringClassNameIndex =
            addConstantUtf8(StringConstants.JAVA_LANG_STRING);

        // Add 'StringBuffer'
        this.stringBufferClassNameIndex =
            addConstantUtf8(StringConstants.JAVA_LANG_STRING_BUFFER);

        // Add 'StringBuilder'
        this.stringBuilderClassNameIndex =
            addConstantUtf8(StringConstants.JAVA_LANG_STRING_BUILDER);

        // Add 'this'
        this.thisLocalVariableNameIndex =
            addConstantUtf8(StringConstants.THIS_LOCAL_VARIABLE_NAME);
    }

    public Constant get(int i)
    {
        return this.listOfConstants.get(i);
    }

    public int size()
    {
        return this.listOfConstants.size();
    }

    // -- Constants -------------------------------------------------------- //

    public int addConstantUtf8(String s)
    {
        if (s == null) {
            throw new IllegalArgumentException("Constant string is null");
        }

        if (s.startsWith("L[")) {
            throw new IllegalArgumentException("Constant string starts with L[");
        }

        int index = this.constantUtf8ToIndex.get(s);

        if (index == -1)
        {
            ConstantUtf8 cutf8 =
                new ConstantUtf8(s);
            index = this.listOfConstants.size();
            this.listOfConstants.add(cutf8);
            this.constantUtf8ToIndex.put(s, index);
        }

        return index;
    }

    public int addConstantClass(int nameIndex)
    {
        String internalName = getConstantUtf8(nameIndex);
        if (internalName == null ||
            internalName.isEmpty() ||
            internalName.charAt(internalName.length()-1) == ';') {
            System.err.println("ConstantPool.addConstantClass: invalid name index");
        }

        int index = this.constantClassToIndex.get(nameIndex);

        if (index == -1)
        {
            ConstantClass cc =
                new ConstantClass(nameIndex);
            index = this.listOfConstants.size();
            this.listOfConstants.add(cc);
            this.constantClassToIndex.put(nameIndex, index);
        }

        return index;
    }

    public int addConstantNameAndType(int nameIndex, int descriptorIndex)
    {
        int index = this.listOfConstants.size();

        while (--index > 0)
        {
            Constant constant = this.listOfConstants.get(index);

            if (constant instanceof ConstantNameAndType) { // to convert to jdk16 pattern matching only when spotbugs #1617 and eclipse #577987 are solved
                ConstantNameAndType cnat = (ConstantNameAndType) constant;
                if (cnat.getNameIndex() == nameIndex &&
                    cnat.getSignatureIndex() == descriptorIndex) {
                    return index;
                }
            }
        }

        ConstantNameAndType cnat = new ConstantNameAndType(nameIndex, descriptorIndex);
        index = this.listOfConstants.size();
        this.listOfConstants.add(cnat);

        return index;
    }

    public int addConstantFieldref(int classIndex, int nameAndTypeIndex)
    {
        int index = this.listOfConstants.size();

        while (--index > 0)
        {
            Constant constant = this.listOfConstants.get(index);

            if (constant instanceof ConstantFieldref) { // to convert to jdk16 pattern matching only when spotbugs #1617 and eclipse #577987 are solved
                ConstantFieldref cfr = (ConstantFieldref) constant;
                if (cfr.getClassIndex() == classIndex &&
                    cfr.getNameAndTypeIndex() == nameAndTypeIndex) {
                    return index;
                }
            }
        }

        ConstantFieldref cfr = new ConstantFieldref(classIndex, nameAndTypeIndex);
        index = this.listOfConstants.size();
        this.listOfConstants.add(cfr);

        return index;
    }

    public int addConstantMethodref(
        int classIndex, int nameAndTypeIndex)
    {
        int index = this.listOfConstants.size();

        while (--index > 0)
        {
            Constant constant = this.listOfConstants.get(index);

            if (constant instanceof ConstantMethodref) { // to convert to jdk16 pattern matching only when spotbugs #1617 and eclipse #577987 are solved
                ConstantMethodref cmr = (ConstantMethodref) constant;
                if (cmr.getClassIndex() == classIndex &&
                    cmr.getNameAndTypeIndex() == nameAndTypeIndex) {
                    return index;
                }
            }
        }

        ConstantMethodref cmr = new ConstantMethodref(classIndex, nameAndTypeIndex);

        index = this.listOfConstants.size();
        this.listOfConstants.add(cmr);

        return index;
    }

    public String getConstantUtf8(int index)
    {
        ConstantUtf8 cutf8 = (ConstantUtf8)this.listOfConstants.get(index);
        return cutf8.getBytes();
    }

    public String getConstantClassName(int index)
    {
        ConstantClass cc = (ConstantClass)this.listOfConstants.get(index);
        ConstantUtf8 cutf8 = (ConstantUtf8)this.listOfConstants.get(cc.getNameIndex());
        return cutf8.getBytes();
    }

    public ConstantClass getConstantClass(int index)
    {
        return (ConstantClass)this.listOfConstants.get(index);
    }

    public ConstantFieldref getConstantFieldref(int index)
    {
        return (ConstantFieldref)this.listOfConstants.get(index);
    }

    public ConstantNameAndType getConstantNameAndType(int index)
    {
        return (ConstantNameAndType)this.listOfConstants.get(index);
    }

    public ConstantCP getConstantMethodref(int index)
    {
        return (ConstantCP)this.listOfConstants.get(index);
    }

    public ConstantMethodType getConstantMethodType(int index)
    {
        return (ConstantMethodType)this.listOfConstants.get(index);
    }
    
    public ConstantMethodHandle getConstantMethodHandle(int index)
    {
        return (ConstantMethodHandle)this.listOfConstants.get(index);
    }
    
    public ConstantInterfaceMethodref getConstantInterfaceMethodref(int index)
    {
        return (ConstantInterfaceMethodref)this.listOfConstants.get(index);
    }

    public ConstantInvokeDynamic getConstantInvokeDynamic(int index)
    {
        return (ConstantInvokeDynamic) this.listOfConstants.get(index);
    }

    public Constant getConstantValue(int index)
    {
        return this.listOfConstants.get(index);
    }

    public int getAppendIndex() {
        return appendIndex;
    }

    public int getClassConstructorIndex() {
        return classConstructorIndex;
    }

    public int getInstanceConstructorIndex() {
        return instanceConstructorIndex;
    }

    public int getInternalDeprecatedSignatureIndex() {
        return internalDeprecatedSignatureIndex;
    }

    public int getObjectClassNameIndex() {
        return objectClassNameIndex;
    }

    public int getObjectSignatureIndex() {
        return objectSignatureIndex;
    }

    public int getStringBufferClassNameIndex() {
        return stringBufferClassNameIndex;
    }

    public int getStringBuilderClassNameIndex() {
        return stringBuilderClassNameIndex;
    }

    public int getToStringIndex() {
        return toStringIndex;
    }

    public int getStringClassNameIndex() {
        return stringClassNameIndex;
    }

    public int getValueOfIndex() {
        return valueOfIndex;
    }

    public int getThisLocalVariableNameIndex() {
        return thisLocalVariableNameIndex;
    }
    
    public String getLocalVariableName(LocalVariable localVariable) {
        return getConstantUtf8(localVariable.getNameIndex());
    }
}
