/**
 * Copyright (C) 2007-2019 Emmanuel Dupuy GPLv3
 * Copyright (C) 2026 Nicolas Baumann (GitHub: nbauma109), with Claude Sonnet 5 (claude-sonnet-5)
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
import org.apache.bcel.classfile.PermittedSubclasses;
import org.apache.bcel.classfile.Record;
import org.apache.bcel.classfile.Signature;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.model.javasyntax.type.AbstractTypeArgumentVisitor;
import org.jd.core.v1.model.javasyntax.type.InnerObjectType;
import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.model.javasyntax.type.Type;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;
import org.jd.core.v1.util.StringConstants;

import jd.core.util.TypeNameUtil;

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
    private Field[] fields;
    private RecordComponent[] recordComponents;
    private Method[] methods;

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
            if (method.isStatic() &&
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
        return TypeNameUtil.localTypeSimpleName(this.thisClassName, this.thisClassName.substring(
            outerClassName.length() + 1));
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

    public void setFields(Field[] fields)
    {
        this.fields = fields;
    }

    public RecordComponent[] getRecordComponents()
    {
        return recordComponents;
    }

    public void setRecordComponents(RecordComponent[] recordComponents)
    {
        this.recordComponents = recordComponents;
    }

    public Method[] getMethods()
    {
        return this.methods;
    }

    public void setMethods(Method[] methods)
    {
        this.methods = methods;
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

    public Record getAttributeRecord()
    {
        for (Attribute attribute : this.getAttributes()) {
            if (attribute.getTag() == Const.ATTR_RECORD) {
                return (Record)attribute;
            }
        }
        return null;
    }

    public PermittedSubclasses getAttributePermittedSubclasses()
    {
        for (Attribute attribute : this.getAttributes()) {
            if (attribute instanceof PermittedSubclasses permittedSubclasses) {
                return permittedSubclasses;
            }
        }
        return null;
    }

    public boolean isSealed()
    {
        // Enums with constant-specific bodies are implicitly sealed in bytecode,
        // but Java source cannot declare them sealed or list their subclasses.
        return (getAccessFlags() & Const.ACC_ENUM) == 0 &&
            getAttributePermittedSubclasses() != null;
    }

    public boolean isNonSealed()
    {
        if (getAttributePermittedSubclasses() != null ||
            (getAccessFlags() & (Const.ACC_FINAL | Const.ACC_ENUM)) != 0 || isRecord()) {
            return false;
        }

        for (ClassFile parent : superClassAndInterfaces.values()) {
            if (parent != null && parent.getAttributePermittedSubclasses() != null) {
                return true;
            }
        }
        return false;
    }

    private boolean isAnonymousClass()
    {
        int index = this.thisClassName.lastIndexOf(
                StringConstants.INTERNAL_INNER_SEPARATOR);

        return index != -1 && index+1 < this.thisClassName.length() && Character.isDigit(this.thisClassName.charAt(index+1))
                && !isLocalNamedStaticType();
    }

    /**
     * A local enum, record or interface is implicitly static and cannot capture
     * anything: it is written as a member type, with the name it was given.
     */
    public boolean isLocalNamedStaticType()
    {
        String simpleName = this.thisClassName.substring(
                this.thisClassName.lastIndexOf(StringConstants.INTERNAL_INNER_SEPARATOR) + 1);
        return !simpleName.equals(TypeNameUtil.localTypeSimpleName(simpleName))
                && ((getAccessFlags() & (Const.ACC_ENUM | Const.ACC_INTERFACE)) != 0 || isRecord())
                && isUniqueLocalTypeName(simpleName) && !referencesOtherLocalType() && !hidesTypeParameter(simpleName);
    }

    /** A type variable of an enclosing class cannot be qualified: a member of the same name would take its place. */
    private boolean hidesTypeParameter(String simpleName)
    {
        // The name written may be the one given to tell the type from a member
        String plainName = TypeNameUtil.localTypeSimpleName(simpleName);
        String writtenName = TypeNameUtil.localTypeSimpleName(this.thisClassName, simpleName);
        for (ClassFile enclosing = this.outerClass; enclosing != null; enclosing = enclosing.outerClass) {
            for (Attribute attribute : enclosing.getAttributes()) {
                if (attribute instanceof Signature signature && declaresTypeParameter(signature.getSignature(), plainName, writtenName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean declaresTypeParameter(String declared, String... names)
    {
        if (!declared.startsWith("<")) {
            return false;
        }
        for (String name : names) {
            if (declared.startsWith("<" + name + ":") || declared.contains(";" + name + ":")) {
                return true;
            }
        }
        return false;
    }

    private static final ThreadLocal<Set<String>> CHECKED_LOCAL_TYPES = ThreadLocal.withInitial(HashSet::new);

    /** A member cannot name a type local to a method: the types the local one refers to must become members too. */
    private boolean referencesOtherLocalType()
    {
        Set<String> checking = CHECKED_LOCAL_TYPES.get();
        if (!checking.add(this.thisClassName)) {
            // Types referring to each other: the answer depends on the other references only
            return false;
        }
        try {
            ConstantPool pool = getConstantPool();
            for (int i = 1; i < pool.size(); i++) {
                if (pool.get(i) instanceof ConstantClass constantClass && isUnpromotableLocalType(pool.getConstantUtf8(constantClass.getNameIndex()))) {
                    return true;
                }
            }
            return false;
        } finally {
            checking.remove(this.thisClassName);
        }
    }

    private boolean isUnpromotableLocalType(String referenced)
    {
        String name = referenced;
        if (name.charAt(0) == '[') {
            name = name.substring(name.indexOf('L') + 1, name.length() - 1);
        }
        int separator = name.lastIndexOf(StringConstants.INTERNAL_INNER_SEPARATOR);
        if (name.equals(this.thisClassName) || separator == -1 || separator + 2 >= name.length()
                || !Character.isDigit(name.charAt(separator + 1))
                || TypeNameUtil.localTypeSimpleName(name.substring(separator + 1)).equals(name.substring(separator + 1))) {
            return false;
        }
        if (this.loader == null) {
            return true;
        }
        ClassFile other = jd.core.process.deserializer.ClassFileDeserializer.deserialize(this.loader, name + StringConstants.CLASS_FILE_SUFFIX, true);
        if (other == null) {
            return true;
        }
        // The type variables of the enclosing classes decide whether it can be promoted
        String prefix = name.substring(0, name.lastIndexOf(StringConstants.INTERNAL_INNER_SEPARATOR));
        if (this.outerClass != null && other.outerClass == null && this.outerClass.thisClassName.equals(prefix)) {
            other.outerClass = this.outerClass;
        }
        return !other.isLocalNamedStaticType();
    }

    /** A member type of the same name would be the one written, wherever the local type is named. */
    private boolean isUniqueLocalTypeName(String simpleName)
    {
        if (this.loader == null) {
            return true;
        }
        String plainName = TypeNameUtil.localTypeSimpleName(simpleName);
        String prefix = this.thisClassName.substring(
                0, this.thisClassName.lastIndexOf(StringConstants.INTERNAL_INNER_SEPARATOR) + 1);
        String member = prefix + plainName;
        if (this.loader.canLoad(member) || this.loader.canLoad(member + StringConstants.CLASS_FILE_SUFFIX)) {
            // A member type already has this name: the local one is written with its number
            TypeNameUtil.registerLocalTypeShadowedByMember(this.thisClassName);
        }
        if (hidesUnnamedPackageType(simpleName) || hidesPackageQualifier(plainName)) {
            return false;
        }
        // The name written must not be the one of another member either
        String written = prefix + TypeNameUtil.localTypeSimpleName(this.thisClassName, simpleName);
        if (this.loader.canLoad(written) || this.loader.canLoad(written + StringConstants.CLASS_FILE_SUFFIX)) {
            return false;
        }
        String writtenSimpleName = written.substring(prefix.length());
        return !namesEnclosingClass(prefix, writtenSimpleName) && !collidesWithSiblingLocalType(prefix, writtenSimpleName);
    }

    /** A nested type cannot be named like one of the classes enclosing it. */
    private static boolean namesEnclosingClass(String prefix, String writtenSimpleName)
    {
        String enclosing = prefix.substring(prefix.lastIndexOf(StringConstants.INTERNAL_PACKAGE_SEPARATOR) + 1);
        return java.util.Arrays.asList(enclosing.split("\\" + StringConstants.INTERNAL_INNER_SEPARATOR)).contains(writtenSimpleName);
    }

    /** A member named like the root of a package would break the qualified names that start with it. */
    private boolean hidesPackageQualifier(String plainName)
    {
        ClassFile root = this;
        while (root.outerClass != null) {
            root = root.outerClass;
        }
        ConstantPool pool = root.getConstantPool();
        String qualifier = plainName + StringConstants.INTERNAL_PACKAGE_SEPARATOR;
        for (int i = 1; i < pool.size(); i++) {
            if (pool.get(i) instanceof org.apache.bcel.classfile.ConstantUtf8 utf8 && startsPackage(utf8.getBytes(), qualifier)) {
                return true;
            }
        }
        return false;
    }

    /** Whether a class name or descriptor contains a qualified name that starts with the qualifier. */
    private static boolean startsPackage(String text, String qualifier)
    {
        for (int at = text.indexOf(qualifier); at != -1; at = text.indexOf(qualifier, at + 1)) {
            if (at == 0 || !Character.isJavaIdentifierPart(text.charAt(at - 1)) && text.charAt(at - 1) != '/'
                    || at == 1 && text.charAt(0) == 'L') {
                return true;
            }
            // Descriptors write the class after an L that follows a delimiter, as in (Lorg/Thing;)
            if (text.charAt(at - 1) == 'L' && (at == 1 || "(;[<>)".indexOf(text.charAt(at - 2)) != -1)) {
                return true;
            }
        }
        return false;
    }

    /** A top-level type of the unnamed package cannot be qualified: the member would take its place. */
    private boolean hidesUnnamedPackageType(String simpleName)
    {
        if (this.thisClassName.indexOf(StringConstants.INTERNAL_PACKAGE_SEPARATOR) != -1) {
            return false;
        }
        for (String top : new String[] {TypeNameUtil.localTypeSimpleName(simpleName), TypeNameUtil.localTypeSimpleName(this.thisClassName, simpleName)}) {
            if (this.loader.canLoad(top) || this.loader.canLoad(top + StringConstants.CLASS_FILE_SUFFIX)) {
                return true;
            }
        }
        return false;
    }

    /** Another local type of the same class may be written with this name. */
    private boolean collidesWithSiblingLocalType(String prefix, String writtenSimpleName)
    {
        List<String> siblings = new java.util.ArrayList<>();
        siblings.add(prefix + 1 + writtenSimpleName);
        for (int i = 1; i < 10; i++)
        {
            if (writtenSimpleName.endsWith("_" + i)) {
                siblings.add(prefix + i + writtenSimpleName.substring(0, writtenSimpleName.length() - 2));
            }
        }
        for (String other : siblings)
        {
            if (!other.equals(this.thisClassName)
                    && (this.loader.canLoad(other) || this.loader.canLoad(other + StringConstants.CLASS_FILE_SUFFIX))) {
                return true;
            }
        }
        return false;
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
            for (ClassFile innerClassFile : innerClassFiles) {
                if (innerClassFile.thisClassName.equals(internalClassName)) {
                    return innerClassFile;
                }
            }
        }

        return null;
    }

    /**
     * Looks up a class, anywhere in the type hierarchy being decompiled
     * (outer classes and their nested classes), by its internal name.
     */
    public ClassFile resolveClassFile(String internalClassName)
    {
        ClassFile root = this;
        while (root.getOuterClass() != null) {
            root = root.getOuterClass();
        }

        return findClassFile(root, internalClassName);
    }

    private static ClassFile findClassFile(ClassFile owner, String internalClassName)
    {
        if (owner.thisClassName.equals(internalClassName)) {
            return owner;
        }

        if (owner.innerClassFiles != null) {
            for (ClassFile child : owner.innerClassFiles) {
                ClassFile result = findClassFile(child, internalClassName);
                if (result != null) {
                    return result;
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

    public ClassFile getSwitchMapOwner(String name)
    {
        for (ClassFile owner = this; owner != null; owner = owner.getOuterClass()) {
            if (owner.getSwitchMaps().containsKey(name)) {
                return owner;
            }
        }
        return null;
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

    public boolean isRecord() {
        return "java/lang/Record".equals(superClassName);
    }
}
