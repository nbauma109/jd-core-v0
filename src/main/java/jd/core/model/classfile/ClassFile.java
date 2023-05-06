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
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.BootstrapMethods;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.InnerClasses;
import org.apache.bcel.classfile.MethodParameters;
import org.apache.bcel.classfile.Signature;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.model.javasyntax.type.AbstractTypeArgumentVisitor;
import org.jd.core.v1.model.javasyntax.type.InnerObjectType;
import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.model.javasyntax.type.Type;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;
import org.jd.core.v1.util.StringConstants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jd.core.model.classfile.accessor.Accessor;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.variable.DefaultVariableNameGenerator;
import jd.core.process.analyzer.variable.VariableNameGenerator;
import jd.core.util.SignatureUtil;

public class ClassFile extends Base
{
    private final int minorVersion;
    private final int majorVersion;
    private final int thisClass;
    private final int superClass;

    private final int[] interfaces;
    private final Field[] fields;
    private final Method[] methods;

    private final ConstantPool constants;
    private final String thisClassName;
    private final String superClassName;
    private final String internalClassName;
    private final String internalPackageName;

    private ClassFile outerClass;
    private final Map<String, ClassFile> superClassAndInterfaces;
    private Field outerThisField;
    private List<ClassFile> innerClassFiles;

    private final Method staticMethod;
    private List<Instruction> enumValues;
    private String internalAnonymousClassName;
    private final Map<String, Map<String, Accessor>> accessors;

    private final VariableNameGenerator variableNameGenerator;

    /**
     * Attention :
     * - Dans le cas des instructions Switch+Enum d'Eclipse, la clé de la map
     *   est l'indexe du nom de la méthode
     *   "static int[] $SWITCH_TABLE$basic$data$TestEnum$enum1()".
     * - Dans le cas des instructions Switch+Enum des autres compilateurs, la
     *   clé de la map est le nom de la classe interne "static class 1"
     *   contenant le tableau de correspondance
     *   "$SwitchMap$basic$data$TestEnum$enum1".
     */
    private final Map<String, List<Integer>> switchMaps;
    
    private final Loader loader;

    public ClassFile(int minorVersion, int majorVersion,
                     ConstantPool constants, int accessFlags, int thisClass,
                     int superClass, int[] interfaces, Field[] fields,
                     Method[] methods, Attribute[] attributes, Loader loader)
    {
        super(accessFlags, attributes);

        this.loader = loader;
        this.minorVersion = minorVersion;
        this.majorVersion = majorVersion;
        this.thisClass = thisClass;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.fields = fields;
        this.methods = methods;

        this.constants = constants;

        // internalClassName
        this.thisClassName =
            this.constants.getConstantClassName(this.thisClass);
        // internalSuperClassName
        this.superClassName = this.superClass == 0 ? null :
            this.constants.getConstantClassName(this.superClass);
        this.internalClassName = SignatureUtil.createTypeName(this.thisClassName);
        // internalPackageName
        int index = this.thisClassName.lastIndexOf(
                StringConstants.INTERNAL_PACKAGE_SEPARATOR);
        this.internalPackageName =
            index == -1 ? "" : this.thisClassName.substring(0, index);

        // staticMethod
        this.staticMethod = findStaticMethod();

        // internalAnonymousClassName
        this.internalAnonymousClassName = null;
        // accessors
        this.accessors = new HashMap<>(10);
        // SwitchMap for Switch+Enum instructions
        this.switchMaps = new HashMap<>();
        this.variableNameGenerator = new DefaultVariableNameGenerator(this);
        this.superClassAndInterfaces = new HashMap<>();
    }

    private Method findStaticMethod() {
        for (Method method : methods)
        {
            if ((method.getAccessFlags() & Const.ACC_STATIC) != 0 &&
                method.getNameIndex() == this.constants.getClassConstructorIndex())
            {
                return method;
            }
        }
        return null;
    }
    
    public ConstantPool getConstantPool()
    {
        return this.constants;
    }

    public int[] getInterfaces()
    {
        return interfaces;
    }

    public int getMajorVersion()
    {
        return majorVersion;
    }

    public int getMinorVersion()
    {
        return minorVersion;
    }

    public int getSuperClassIndex()
    {
        return superClass;
    }

    public int getThisClassIndex()
    {
        return thisClass;
    }

    public String getClassName()
    {
        if (this.outerClass == null)
        {
            // int index = this.thisClassName.lastIndexOf(
            //   AnalyzerConstants.INTERNAL_INNER_SEPARATOR);
            //if (index != -1)
            //    return this.thisClassName.substring(index+1);

            int index = this.thisClassName.lastIndexOf(
                    StringConstants.INTERNAL_PACKAGE_SEPARATOR);
            return index == -1 ?
                this.thisClassName :
                this.thisClassName.substring(index + 1);
        }
        String outerClassName = this.outerClass.getThisClassName();
        return this.thisClassName.substring(
            outerClassName.length() + 1);
    }

    public String getThisClassName()
    {
        return this.thisClassName;
    }

    public String getSuperClassName()
    {
        return this.superClassName;
    }

    public String getInternalClassName()
    {
        return this.internalClassName;
    }

    public String getInternalPackageName()
    {
        return this.internalPackageName;
    }

    public Field[] getFields()
    {
        return this.fields;
    }

    public Method[] getMethods()
    {
        return this.methods;
    }

    public Method getMethod(int i)
    {
        return this.methods[i];
    }

    public InnerClasses getAttributeInnerClasses()
    {
        for (Attribute attribute : this.getAttributes()) {
            if (attribute.getTag() == Const.ATTR_INNER_CLASSES) {
                return (InnerClasses)attribute;
            }
        }
        return null;
    }

    public BootstrapMethods getAttributeBootstrapMethods()
    {
        for (Attribute attribute : this.getAttributes()) {
            if (attribute.getTag() == Const.ATTR_BOOTSTRAP_METHODS) {
                return (BootstrapMethods)attribute;
            }
        }
        return null;
    }
    
    public MethodParameters getAttributeMethodParameters()
    {
        for (Attribute attribute : this.getAttributes()) {
            if (attribute.getTag() == Const.ATTR_METHOD_PARAMETERS) {
                return (MethodParameters)attribute;
            }
        }
        return null;
    }
    
    private boolean isAnonymousClass()
    {
        int index = this.thisClassName.lastIndexOf(
                StringConstants.INTERNAL_INNER_SEPARATOR);

        return index != -1 && index+1 < this.thisClassName.length() && Character.isDigit(this.thisClassName.charAt(index+1));
    }

    public boolean isAInnerClass()
    {
        return this.outerClass != null;
    }

    public ClassFile getOuterClass()
    {
        return outerClass;
    }

    public void setOuterClass(ClassFile outerClass)
    {
        this.outerClass = outerClass;

        // internalAnonymousClassName
        if (isAnonymousClass())
        {
            ConstantClass cc = this.constants.getConstantClass(this.superClass);

            if (cc.getNameIndex() != this.constants.getObjectClassNameIndex())
            {
                // Super class
                this.internalAnonymousClassName = this.superClassName;
            }
            else if (this.interfaces != null && this.interfaces.length > 0)
            {
                // Interface
                int interfaceIndex = this.interfaces[0];
                this.internalAnonymousClassName =
                    this.constants.getConstantClassName(interfaceIndex);
            }
            else
            {
                this.internalAnonymousClassName = StringConstants.JAVA_LANG_OBJECT;
            }
        }
        else
        {
            this.internalAnonymousClassName = null;
        }
    }

    public Field getOuterThisField()
    {
        return outerThisField;
    }

    public void setOuterThisField(Field outerThisField)
    {
        this.outerThisField = outerThisField;
    }

    public List<ClassFile> getInnerClassFiles()
    {
        return innerClassFiles;
    }

    public void setInnerClassFiles(List<ClassFile> innerClassFiles)
    {
        this.innerClassFiles = innerClassFiles;
    }

    public ClassFile getInnerClassFile(String internalClassName)
    {
        if (this.innerClassFiles != null &&
            internalClassName.length() > this.thisClassName.length()+1 &&
            internalClassName.charAt(this.thisClassName.length()) == StringConstants.INTERNAL_INNER_SEPARATOR)
        {
            for (int i=this.innerClassFiles.size()-1; i>=0; --i) {
                if (innerClassFiles.get(i).thisClassName.equals(internalClassName)) {
                    return innerClassFiles.get(i);
                }
            }
        }

        return null;
    }

    public Field getField(int fieldNameIndex, int fieldDescriptorIndex)
    {
        for (Field field : fields)
        {
            if (fieldNameIndex == field.getNameIndex() &&
                fieldDescriptorIndex == field.getDescriptorIndex())
            {
                return field;
            }
        }
        return null;
    }

    public Field getField(String fieldName, String fieldDescriptor)
    {
        String name;
        for (Field field : fields)
        {
            name = this.constants.getConstantUtf8(field.getNameIndex());

            if (fieldName.equals(name))
            {
                String descriptor =
                    this.constants.getConstantUtf8(field.getDescriptorIndex());

                if (fieldDescriptor.equals(descriptor)) {
                    return field;
                }
            }
        }
        return null;
    }

    public Method getStaticMethod()
    {
        return staticMethod;
    }

    public Method getMethod(String methodName, String methodDescriptor)
    {
        String name;
        for (Method method : methods)
        {
            name = this.constants.getConstantUtf8(method.getNameIndex());

            if (methodName.equals(name))
            {
                String descriptor =
                    this.constants.getConstantUtf8(method.getDescriptorIndex());
                Signature attributeSignature = method.getAttributeSignature();
                if (methodDescriptor.equals(descriptor)) {
                    return method;
                }
                if (attributeSignature != null) {
                    String signature = attributeSignature.getSignature();
                    int nbrOfParameters1 = SignatureUtil.getParameterSignatureCount(methodDescriptor);
                    int nbrOfParameters2 = SignatureUtil.getParameterSignatureCount(descriptor);
                    if (nbrOfParameters1 == nbrOfParameters2) {
                        List<String> methodParameters = SignatureUtil.getParameterSignatures(methodDescriptor);
                        List<String> genericParameters = SignatureUtil.getParameterSignatures(signature);
                        if (methodParameters.equals(genericParameters) ||
                        genericParameters.stream().allMatch(SignatureUtil::isGenericSignature)
                         && methodParameters.stream().noneMatch(SignatureUtil::isPrimitiveSignature)) {
                            return method;
                        }
                    }
                }
            }
        }
        return findMethodInSuperClassAndInterfaces(methodName, methodDescriptor);
    }

    public Method findMethodInSuperClassAndInterfaces(String methodName, String methodDescriptor)
    {
        for (ClassFile classFile : superClassAndInterfaces.values()) {
            Method method = classFile.getMethod(methodName, methodDescriptor);
            if (method != null) {
                return method;
            }
            
        }
        return null;
    }

    public List<Instruction> getEnumValues()
    {
        return enumValues;
    }

    public void setEnumValues(List<Instruction> enumValues)
    {
        this.enumValues = enumValues;
    }

    public String getInternalAnonymousClassName()
    {
        return internalAnonymousClassName;
    }

    public void addAccessor(String name, String descriptor, Accessor accessor)
    {
        this.accessors.computeIfAbsent(name, k -> new HashMap<>(1)).put(descriptor, accessor);
    }

    public Accessor getAccessor(String name, String descriptor)
    {
        Map<String, Accessor> map = this.accessors.get(name);
        return map == null ? null : map.get(descriptor);
    }

    public Map<String, List<Integer>> getSwitchMaps()
    {
        return this.switchMaps;
    }

    public Loader getLoader() {
        return loader;
    }

    public VariableNameGenerator getVariableNameGenerator() {
        return variableNameGenerator;
    }

    public Map<String, ClassFile> getSuperClassAndInterfaces() {
        return superClassAndInterfaces;
    }

    @Override
    public String toString() {
        return internalClassName;
    }

    public Set<String> getTypeArgumentInnerClasses() {
        Set<String> typeArgumentInnerClasses = new HashSet<>();
        Signature signature = this.getAttributeSignature();
        if (signature != null) {
            TypeMaker typeMaker = new TypeMaker(loader);
            Type type = typeMaker.makeFromSignature(signature.getSignature());
            if (type instanceof ObjectType ot) {
                ot.accept(new AbstractTypeArgumentVisitor() {
                    @Override
                    public void visit(InnerObjectType type) {
                        typeArgumentInnerClasses.add(type.getInternalName());
                    }
                });
            }
        }
        return typeArgumentInnerClasses;
    }
}
