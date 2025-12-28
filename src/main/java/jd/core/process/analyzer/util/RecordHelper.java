/*******************************************************************************
 * Copyright (C) 2025 GPLv3
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
package jd.core.process.analyzer.util;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.Method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.BootstrapMethod;
import org.apache.bcel.classfile.BootstrapMethods;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantInvokeDynamic;
import org.apache.bcel.classfile.ConstantMethodHandle;
import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.INVOKEDYNAMIC;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.POP2;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.generic.Type;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.ExceptionUtil;

import static org.jd.core.v1.util.StringConstants.INSTANCE_CONSTRUCTOR;

public final class RecordHelper {

    private RecordHelper() {
    }

    /* ============================================================
     * Entry points
     * ============================================================ */

    public static Method[] removeImplicitDefaultRecordMethods(ClassFile ownerClass) {
        if (ownerClass == null) {
            return new Method[0];
        }

        Method[] methods = ownerClass.getMethods();
        List<Method> recordMethods = toMutableList(methods);

        ConstantPool constantPool = ownerClass.getConstantPool();
        Map<String, String> recordComponentNamesByFieldName =
                buildFieldNameToComponentNameMap(ownerClass, constantPool);

        removeImplicitDefaultRecordMethods(
                recordMethods,
                ownerClass,
                constantPool,
                recordComponentNamesByFieldName
        );

        stripImplicitRecordConstructorTailAssignments(
                recordMethods,
                ownerClass,
                constantPool
        );

        return recordMethods.toArray(Method[]::new);
    }

    public static void removeImplicitDefaultRecordMethods(
            List<Method> methods,
            ClassFile ownerClass,
            ConstantPool constantPool,
            Map<String, String> recordComponentNamesByFieldName) {

        if (methods == null
                || methods.isEmpty()
                || ownerClass == null
                || constantPool == null
                || recordComponentNamesByFieldName == null) {
            return;
        }

        BootstrapMethods bootstrapMethods =
                findBootstrapMethodsAttribute(ownerClass);

        for (Iterator<Method> iterator = methods.iterator(); iterator.hasNext();) {
            Method method = iterator.next();
            if (matchesDefaultImplicitRecordDefinition(
                    method,
                    ownerClass,
                    bootstrapMethods,
                    constantPool,
                    recordComponentNamesByFieldName)) {
                iterator.remove();
            }
        }
    }

    /* ============================================================
     * Default implicit method detection
     * ============================================================ */

    private static boolean matchesDefaultImplicitRecordDefinition(
            Method method,
            ClassFile ownerClass,
            BootstrapMethods bootstrapMethods,
            ConstantPool constantPool,
            Map<String, String> recordComponentNamesByFieldName) {

        if (method == null) {
            return false;
        }

        return isStrictImplicitCanonicalConstructor(
                method,
                ownerClass,
                constantPool)
        || isImplicitObjectMethodsBootstrap(
                method,
                bootstrapMethods,
                constantPool)
        || isImplicitRecordComponentAccessor(
                method,
                constantPool,
                recordComponentNamesByFieldName);
    }

    private static boolean isStrictImplicitCanonicalConstructor(
            Method method,
            ClassFile ownerClass,
            ConstantPool constantPool) {

        if (!INSTANCE_CONSTRUCTOR.equals(method.getName(constantPool))
                || method.isStatic()
                || !method.isPublic()) {
            return false;
        }

        List<Field> recordFields = getRecordInstanceFields(ownerClass);
        if (recordFields.isEmpty()) {
            return false;
        }

        String expectedSignature =
                buildCanonicalConstructorSignature(recordFields, constantPool);
        if (!expectedSignature.equals(method.getSignature(constantPool))) {
            return false;
        }

        InstructionList instructionList = toInstructionList(method);
        if (instructionList == null) {
            return false;
        }

        try {
            List<Instruction> instructions =
                    nonNopInstructions(instructionList);

            int expectedInstructionCount =
                    3 * recordFields.size() + 3;
            if (instructions.size() != expectedInstructionCount) {
                return false;
            }

            int index = 0;

            if (!isAload0(instructions.get(index))) {
                return false;
            }
            index++;

            Instruction superInit = instructions.get(index);
            if (!(superInit instanceof INVOKESPECIAL invokespecial)
                    || !isRecordSuperInit(invokespecial, constantPool)) {
                return false;
            }
            index++;

            int[] paramSlots =
                    computeConstructorParameterSlots(method, constantPool);

            for (int fieldIndex = 0;
                 fieldIndex < recordFields.size();
                 fieldIndex++) {

                Field field = recordFields.get(fieldIndex);

                if (!isAload0(instructions.get(index))) {
                    return false;
                }
                index++;

                Instruction load = instructions.get(index);
                if (!(load instanceof LoadInstruction loadInstruction)
                        || loadInstruction.getIndex() != paramSlots[fieldIndex]) {
                    return false;
                }
                index++;

                Instruction put = instructions.get(index);
                if (!(put instanceof PUTFIELD putfield)) {
                    return false;
                }

                ConstantFieldref cfr = constantPool.getConstantFieldref(putfield.getIndex());
                ConstantNameAndType cnat = constantPool.getConstantNameAndType(cfr.getNameAndTypeIndex());
                String putFieldName = constantPool.getConstantUtf8(cnat.getNameIndex());
                String putFieldSignature = constantPool.getConstantUtf8(cnat.getSignatureIndex());

                if (!field.getName(constantPool).equals(putFieldName)
                        || !field.getSignature(constantPool).equals(putFieldSignature)) {
                    return false;
                }
                index++;
            }

            return instructions.get(index) instanceof ReturnInstruction;
        } finally {
            instructionList.dispose();
        }
    }

    private static boolean isRecordSuperInit(
            INVOKESPECIAL invokespecial,
            ConstantPool constantPool) {

        ConstantCP cmr = constantPool.getConstantMethodref(invokespecial.getIndex());
        String className = constantPool.getConstantClassName(cmr.getClassIndex());
        ConstantNameAndType cnat = constantPool.getConstantNameAndType(cmr.getNameAndTypeIndex());
        String methodName = constantPool.getConstantUtf8(cnat.getNameIndex());
        String signature = constantPool.getConstantUtf8(cnat.getSignatureIndex());

        return "java/lang/Record".equals(className)
                && INSTANCE_CONSTRUCTOR.equals(methodName)
                && "()V".equals(signature);
    }

    /* ============================================================
     * Constructor tail stripping
     * ============================================================ */

    private static void stripImplicitRecordConstructorTailAssignments(
            List<Method> methods,
            ClassFile ownerClass,
            ConstantPool constantPool) {

        if (methods == null || methods.isEmpty()) {
            return;
        }

        List<Field> recordFields = getRecordInstanceFields(ownerClass);
        if (recordFields.isEmpty()) {
            return;
        }

        String expectedConstructorSignature =
                buildCanonicalConstructorSignature(recordFields, constantPool);

        for (int i = 0; i < methods.size(); i++) {
            Method method = methods.get(i);
            if (!isConstructor(method, expectedConstructorSignature, constantPool)) {
                continue;
            }

            Method updated =
                    removeImplicitConstructorTailPutFields(
                            method,
                            ownerClass,
                            constantPool,
                            recordFields);

            methods.set(i, updated);
        }
    }

    private static boolean isConstructor(
            Method method,
            String expectedConstructorSignature,
            ConstantPool cp) {

        if (method == null
                || !INSTANCE_CONSTRUCTOR.equals(method.getName(cp))
                || method.isStatic()
                || !method.isPublic()) {
            return false;
        }
        return expectedConstructorSignature.equals(method.getSignature(cp));
    }

    /**
     * Removes the compiler-synthesized trailing block:
     * for each record component field in declared order:
     *   aload_0
     *   load <corresponding parameter slot>
     *   putfield <that field>
     * followed by return
     *
     * Only removes if the method ends with a perfect match of that pattern.
     */
    private static Method removeImplicitConstructorTailPutFields(
            Method constructor,
            ClassFile ownerClass,
            ConstantPool constantPool,
            List<Field> recordFields) {

        ConstantPoolGen constantPoolGen =
                new ConstantPoolGen(constantPool.getConstantPool());
        MethodGen methodGen =
                new MethodGen(
                        constructor.getMethod(),
                        ownerClass.getClassName(),
                        constantPoolGen);

        InstructionList editable =
                methodGen.getInstructionList();
        if (editable == null) {
            return constructor;
        }

        List<InstructionHandle> effective =
                nonNopInstructionHandles(editable);
        if (effective.isEmpty()) {
            return constructor;
        }

        InstructionHandle last =
                effective.get(effective.size() - 1);
        if (!(last.getInstruction() instanceof ReturnInstruction)) {
            return constructor;
        }

        int[] paramSlots =
                computeConstructorParameterSlots(constructor, constantPool);

        int cursor = effective.size() - 2;
        List<InstructionHandle> putfieldHandles =
                new ArrayList<>();

        for (int fieldIndex = recordFields.size() - 1;
             fieldIndex >= 0;
             fieldIndex--) {

            if (cursor < 2) {
                return constructor;
            }

            InstructionHandle putHandle =
                    effective.get(cursor);
            InstructionHandle loadHandle =
                    effective.get(cursor - 1);
            InstructionHandle aloadHandle =
                    effective.get(cursor - 2);

            if (!(putHandle.getInstruction() instanceof PUTFIELD putfield)
                    || !(loadHandle.getInstruction() instanceof LoadInstruction loadInstruction)
                    || !isAload0(aloadHandle.getInstruction())) {
                return constructor;
            }

            Field field = recordFields.get(fieldIndex);

            ConstantFieldref cfr = constantPool.getConstantFieldref(putfield.getIndex());
            ConstantNameAndType cnat = constantPool.getConstantNameAndType(cfr.getNameAndTypeIndex());
            String putFieldName = constantPool.getConstantUtf8(cnat.getNameIndex());
            String putFieldSignature = constantPool.getConstantUtf8(cnat.getSignatureIndex());

            if (!field.getName(constantPool).equals(putFieldName)
                    || !field.getSignature(constantPool).equals(putFieldSignature)) {
                return constructor;
            }

            int expectedSlot = paramSlots[fieldIndex];
            int actualSlot = loadInstruction.getIndex();
            if (expectedSlot != actualSlot) {
                return constructor;
            }

            putfieldHandles.add(putHandle);
            cursor -= 3;
        }

        InstructionHandle firstToDelete =
                effective.get(cursor + 1);
        InstructionHandle lastToDelete =
                effective.get(effective.size() - 2);

        if (canDeleteRange(firstToDelete, lastToDelete)) {
            try {
                editable.delete(firstToDelete, lastToDelete);
            } catch (TargetLostException e) {
                assert ExceptionUtil.printStackTrace(e);
            }
        } else {
            neutralizePutFields(editable, putfieldHandles, constantPool);
        }

        methodGen.setInstructionList(editable);
        methodGen.setMaxStack();
        methodGen.setMaxLocals();

        return new Method(methodGen.getMethod());
    }

    private static boolean canDeleteRange(
            InstructionHandle firstToDelete,
            InstructionHandle lastToDelete) {

        if (firstToDelete == null || lastToDelete == null) {
            return false;
        }

        for (InstructionHandle h = firstToDelete; h != null; h = h.getNext()) {
            if (h.hasTargeters()) {
                return false;
            }
            if (h == lastToDelete) {
                return true;
            }
        }

        return false;
    }

    private static void neutralizePutFields(
            InstructionList editable,
            List<InstructionHandle> putfieldHandles,
            ConstantPool constantPool) {

        if (editable == null
                || putfieldHandles == null
                || putfieldHandles.isEmpty()) {
            return;
        }

        for (InstructionHandle putHandle : putfieldHandles) {
            if (!(putHandle.getInstruction() instanceof PUTFIELD putfield)) {
                continue;
            }

            ConstantFieldref cfr = constantPool.getConstantFieldref(putfield.getIndex());
            ConstantNameAndType cnat = constantPool.getConstantNameAndType(cfr.getNameAndTypeIndex());
            Type fieldType =
                    Type.getType(
                            constantPool.getConstantUtf8(cnat.getSignatureIndex()));
            boolean needsPop2 =
                    fieldType == Type.LONG
                            || fieldType == Type.DOUBLE;

            putHandle.setInstruction(
                    needsPop2 ? new POP2() : new POP());

            /*
             * We still have objectref on stack from the preceding aload_0.
             * Insert a POP after the value-pop instruction to remove objectref too.
             */
            editable.insert(putHandle, new POP());
        }
    }

    /* ============================================================
     * Utilities
     * ============================================================ */

    private static int[] computeConstructorParameterSlots(Method constructor, ConstantPool cp) {
        Type[] args =
                Type.getArgumentTypes(constructor.getSignature(cp));
        int[] slots = new int[args.length];

        int slot = 1;
        for (int i = 0; i < args.length; i++) {
            slots[i] = slot;
            slot += args[i].getSize();
        }

        return slots;
    }

    private static Map<String, String> buildFieldNameToComponentNameMap(
            ClassFile ownerClass, ConstantPool cp) {

        Map<String, String> map = new HashMap<>();
        Field[] fields = ownerClass.getFields();

        if (fields == null || fields.length == 0) {
            return map;
        }

        for (Field field : fields) {
            if (field == null
                    || field.isStatic()
                    || !field.isPrivate()
                    || !field.isFinal()) {
                continue;
            }

            String name = field.getName(cp);
            if (name == null || name.isEmpty()) {
                continue;
            }

            map.put(name, name);
        }

        return map;
    }

    private static List<Field> getRecordInstanceFields(
            ClassFile ownerClass) {

        List<Field> result = new ArrayList<>();
        Field[] fields = ownerClass.getFields();

        if (fields == null || fields.length == 0) {
            return result;
        }

        for (Field field : fields) {
            if (field == null
                    || field.isStatic()
                    || !field.isPrivate()
                    || !field.isFinal()) {
                continue;
            }
            result.add(field);
        }

        return result;
    }

    private static String buildCanonicalConstructorSignature(
            List<Field> recordFields, ConstantPool cp) {

        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (Field field : recordFields) {
            sb.append(field.getSignature(cp));
        }
        sb.append(")V");
        return sb.toString();
    }

    private static BootstrapMethods findBootstrapMethodsAttribute(
            ClassFile ownerClass) {

        Attribute[] attributes = ownerClass.getAttributes();
        if (attributes == null || attributes.length == 0) {
            return null;
        }

        for (Attribute attribute : attributes) {
            if (attribute instanceof BootstrapMethods bootstrapMethods) {
                return bootstrapMethods;
            }
        }

        return null;
    }

    private static boolean isImplicitObjectMethodsBootstrap(
            Method method,
            BootstrapMethods bootstrapMethods,
            ConstantPool constantPool) {

        if (bootstrapMethods == null
                || !isEqualsHashCodeToStringSignature(method, constantPool)) {
            return false;
        }

        InstructionList instructionList =
                toInstructionList(method);
        if (instructionList == null) {
            return false;
        }

        try {
            for (InstructionHandle handle = instructionList.getStart();
                 handle != null;
                 handle = handle.getNext()) {

                Instruction instruction =
                        handle.getInstruction();
                if (instruction instanceof INVOKEDYNAMIC invokedynamic
                        && referencesObjectMethodsBootstrap(
                                invokedynamic,
                                bootstrapMethods,
                                constantPool)) {
                    return true;
                }
            }
            return false;
        } finally {
            instructionList.dispose();
        }
    }

    private static boolean isEqualsHashCodeToStringSignature(Method method, ConstantPool cp) {
        String name = method.getName(cp);
        String signature = method.getSignature(cp);

        return isToString(name, signature)
                || isHashCode(name, signature)
                || isEquals(name, signature);
    }

    private static boolean isEquals(String name, String signature) {
        return "equals".equals(name)
                && "(Ljava/lang/Object;)Z".equals(signature);
    }

    private static boolean isHashCode(String name, String signature) {
        return "hashCode".equals(name)
                && "()I".equals(signature);
    }

    private static boolean isToString(String name, String signature) {
        return "toString".equals(name)
                && "()Ljava/lang/String;".equals(signature);
    }

    private static boolean referencesObjectMethodsBootstrap(
            INVOKEDYNAMIC invokedynamic,
            BootstrapMethods bootstrapMethods,
            ConstantPool constantPool) {

        Constant invokedynamicConstant =
                constantPool.getConstantValue(invokedynamic.getIndex());
        if (!(invokedynamicConstant instanceof ConstantInvokeDynamic cid)) {
            return false;
        }

        int bootstrapIndex =
                cid.getBootstrapMethodAttrIndex();

        BootstrapMethod[] bootstrapMethodEntries =
                bootstrapMethods.getBootstrapMethods();
        if (bootstrapMethodEntries == null
                || bootstrapIndex < 0
                || bootstrapIndex >= bootstrapMethodEntries.length) {
            return false;
        }

        BootstrapMethod bootstrapMethod =
                bootstrapMethodEntries[bootstrapIndex];
        int bootstrapMethodRef =
                bootstrapMethod.getBootstrapMethodRef();

        Constant methodHandleConstant =
                constantPool.getConstantValue(bootstrapMethodRef);
        if (!(methodHandleConstant instanceof ConstantMethodHandle methodHandle)) {
            return false;
        }

        int referenceIndex =
                methodHandle.getReferenceIndex();

        Constant referenceConstant =
                constantPool.getConstantValue(referenceIndex);
        if (!(referenceConstant instanceof ConstantMethodref methodRef)) {
            return false;
        }

        Constant ownerClassConstant =
                constantPool.getConstantValue(methodRef.getClassIndex());
        if (!(ownerClassConstant instanceof ConstantClass ownerClass)) {
            return false;
        }

        Constant ownerNameConstant =
                constantPool.getConstantValue(ownerClass.getNameIndex());
        if (!(ownerNameConstant instanceof ConstantUtf8)) {
            return false;
        }

        String ownerInternalName =
                ((ConstantUtf8) ownerNameConstant).getBytes();

        return "java/lang/runtime/ObjectMethods".equals(ownerInternalName);
    }

    private static InstructionList toInstructionList(Method method) {
        byte[] code = method.getCode();
        if (code == null) {
            return null;
        }
        return new InstructionList(code);
    }

    private static boolean isImplicitRecordComponentAccessor(
            Method method,
            ConstantPool constantPool,
            Map<String, String> recordComponentNamesByFieldName) {

        if (method.isStatic() || !method.isPublic()) {
            return false;
        }

        String methodName = method.getName(constantPool);
        String methodSignature = method.getSignature(constantPool);

        InstructionList instructionList =
                toInstructionList(method);
        if (instructionList == null) {
            return false;
        }

        try {
            List<Instruction> instructions =
                    nonNopInstructions(instructionList);

            if (instructions.size() != 3
                    || !isAload0(instructions.get(0))
                    || !(instructions.get(1) instanceof GETFIELD)
                    || !(instructions.get(2) instanceof ReturnInstruction)) {
                return false;
            }

            GETFIELD getfield =
                    (GETFIELD) instructions.get(1);
            ConstantFieldref cfr = constantPool.getConstantFieldref(getfield.getIndex());
            ConstantNameAndType cnat = constantPool.getConstantNameAndType(cfr.getNameAndTypeIndex());
            String fieldName = constantPool.getConstantUtf8(cnat.getNameIndex());
            String fieldSignature = constantPool.getConstantUtf8(cnat.getSignatureIndex());

            String componentName =
                    recordComponentNamesByFieldName.get(fieldName);
            if (componentName == null
                    || !componentName.equals(methodName)) {
                return false;
            }

            String expectedSignature =
                    accessorMethodSignatureFromFieldSignature(
                            fieldSignature);
            return expectedSignature.equals(methodSignature);
        } finally {
            instructionList.dispose();
        }
    }

    private static boolean isAload0(Instruction instruction) {
        if (!(instruction instanceof ALOAD)) {
            return false;
        }
        return ((ALOAD) instruction).getIndex() == 0;
    }

    private static String accessorMethodSignatureFromFieldSignature(
            String fieldSignature) {
        return "()" + fieldSignature;
    }

    private static List<Instruction> nonNopInstructions(
            InstructionList instructionList) {

        List<Instruction> result = new ArrayList<>();
        for (InstructionHandle handle = instructionList.getStart();
             handle != null;
             handle = handle.getNext()) {

            Instruction instruction =
                    handle.getInstruction();
            if (!(instruction instanceof NOP)) {
                result.add(instruction);
            }
        }
        return result;
    }

    private static List<InstructionHandle> nonNopInstructionHandles(
            InstructionList instructionList) {

        List<InstructionHandle> result = new ArrayList<>();
        for (InstructionHandle handle = instructionList.getStart();
             handle != null;
             handle = handle.getNext()) {

            Instruction instruction =
                    handle.getInstruction();
            if (!(instruction instanceof NOP)) {
                result.add(handle);
            }
        }
        return result;
    }

    public static List<Method> toMutableList(Method[] methods) {
        List<Method> result = new ArrayList<>();
        if (methods != null && methods.length > 0) {
            Collections.addAll(result, methods);
        }
        return result;
    }
}
