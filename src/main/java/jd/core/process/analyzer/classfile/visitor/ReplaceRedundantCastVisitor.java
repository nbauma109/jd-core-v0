/*******************************************************************************
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
package jd.core.process.analyzer.classfile.visitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.JavaClass;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.util.StringConstants;
import org.jd.core.v1.model.javasyntax.type.BaseTypeArgument;
import org.jd.core.v1.model.javasyntax.type.BaseTypeParameter;
import org.jd.core.v1.model.javasyntax.type.GenericType;
import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.model.javasyntax.type.Type;
import org.jd.core.v1.model.javasyntax.type.TypeArgument;
import org.jd.core.v1.model.javasyntax.type.TypeParameter;
import org.jd.core.v1.model.javasyntax.type.WildcardExtendsTypeArgument;
import org.jd.core.v1.model.javasyntax.type.WildcardSuperTypeArgument;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker.MethodTypes;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker.TypeTypes;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.AConstNull;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.AssertInstruction;
import jd.core.model.instruction.bytecode.instruction.ArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.ArrayStoreInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.IfCmp;
import jd.core.model.instruction.fast.instruction.FastSwitch;
import jd.core.model.instruction.fast.instruction.FastSynchronized;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.InstanceOf;
import jd.core.model.instruction.bytecode.instruction.Switch;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.GetField;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.InvokeNoStaticInstruction;
import jd.core.model.instruction.bytecode.instruction.attribute.ObjectrefAttribute;
import jd.core.process.analyzer.instruction.fast.visitor.AbstractReplaceInstructionVisitor;

/**
 * Removes the cast that the compiler inserts after the call of a method returning a type
 * variable of its class, when the type argument of the receiver is the type of the cast:
 * {@code (String) list.get(0)} with a {@code List<String>}.
 */
public class ReplaceRedundantCastVisitor extends AbstractReplaceInstructionVisitor<Instruction>
{
    private final ClassFile classFile;
    private final LocalVariables localVariables;
    private final TypeMaker typeMaker;
    /** Arguments of the calls met so far: their static type takes part in the choice of an overload. */
    private final Set<Instruction> callArguments = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Instruction, Instruction> parentOf = new IdentityHashMap<>();
    private final Map<Instruction, InvokeInstruction> callOfArgument = new IdentityHashMap<>();

    public ReplaceRedundantCastVisitor(ClassFile classFile, LocalVariables localVariables)
    {
        this.classFile = classFile;
        this.localVariables = localVariables;
        this.typeMaker = new TypeMaker(classFile.getLoader());
    }

    @Override
    public void visit(Instruction instruction)
    {
        if (instruction instanceof InvokeInstruction call) {
            callArguments.addAll(call.getArgs());
            call.getArgs().forEach(argument -> callOfArgument.put(argument, call));
        }
        super.visit(instruction);
    }

    @Override
    protected Instruction newInstruction(Instruction i, Instruction item) {
        return item;
    }

    @Override
    protected Instruction match(Instruction parentFound, Instruction instruction)
    {
        parentOf.put(instruction, parentFound);
        // A member of the result is selected: the compiler inserts the cast again. Elsewhere it may be a runtime check.
        if (localVariables != null && (parentFound instanceof InvokeNoStaticInstruction || parentFound instanceof GetField) && ((ObjectrefAttribute) parentFound).getObjectref() == instruction
            && instruction instanceof CheckCast cast
            && cast.getObjectref() instanceof InvokeNoStaticInstruction invoke && isRedundant(parentFound, cast, invoke))
        {
            return invoke;
        }
        // An argument already has the type of the parameter: the compiler only erased its type variable
        if (parentFound instanceof InvokeNoStaticInstruction call && instruction instanceof CheckCast cast
            && cast.getObjectref() instanceof InvokeNoStaticInstruction invoke && isRedundantArgument(call, cast, invoke))
        {
            return invoke;
        }
        return null;
    }

    /**
     * {@code list.add((Collection) factory.get())} with a {@code List<R>} and a {@code Factory<R>}:
     * the cast is the erasure of the type variable {@code R}, which is already the type of the argument.
     */
    private boolean isRedundantArgument(InvokeNoStaticInstruction call, CheckCast cast, InvokeNoStaticInstruction invoke)
    {
        int index = -1;
        for (int i = 0; i < call.getArgs().size(); i++) {
            if (call.getArgs().get(i) == cast) {
                index = i;
            }
        }
        if (index < 0 || isGenericOrOverloaded(call)) {
            return false;
        }

        // Type of the value, as seen from the receiver of the call that produces it
        TypeArgument valueType = returnedTypeArgument(invoke);
        if (!(valueType instanceof GenericType)) {
            return false;
        }

        // Type of the parameter, as seen from the receiver of the call that takes it
        ConstantPool constants = classFile.getConstantPool();
        ConstantCP methodref = constants.getConstantMethodref(call.getIndex());
        ConstantNameAndType nameAndType = constants.getConstantNameAndType(methodref.getNameAndTypeIndex());
        String owner = constants.getConstantClassName(methodref.getClassIndex());
        MethodTypes methodTypes = typeMaker.makeMethodTypes(owner,
            constants.getConstantUtf8(nameAndType.getNameIndex()), constants.getConstantUtf8(nameAndType.getSignatureIndex()));
        // The argument exists, so does the parameter. An array of a type variable is not the type variable: the cast is kept
        if (methodTypes == null
            || !(methodTypes.getParameterTypes().getList().get(index) instanceof GenericType parameter)
            || parameter.getDimension() != 0 || declaresTypeParameter(methodTypes, parameter.getName()))
        {
            return false;
        }
        return receiverType(call) instanceof ObjectType receiverType && owner.equals(receiverType.getInternalName())
            && receiverType.getTypeArguments() != null
            && valueType.equals(typeArgument(owner, parameter.getName(), receiverType.getTypeArguments()));
    }

    private Type receiverType(InvokeNoStaticInstruction call)
    {
        String signature = call.getObjectref().getReturnedSignature(classFile, localVariables);
        return signature == null || signature.indexOf('<') == -1 ? null : typeMaker.makeFromSignature(signature);
    }

    /** The returned type of a call selecting a type variable of the class of its receiver, as the receiver parameterizes it. */
    private TypeArgument returnedTypeArgument(InvokeNoStaticInstruction invoke)
    {
        ConstantPool constants = classFile.getConstantPool();
        ConstantCP methodref = constants.getConstantMethodref(invoke.getIndex());
        ConstantNameAndType nameAndType = constants.getConstantNameAndType(methodref.getNameAndTypeIndex());
        String owner = constants.getConstantClassName(methodref.getClassIndex());
        MethodTypes methodTypes = typeMaker.makeMethodTypes(owner,
            constants.getConstantUtf8(nameAndType.getNameIndex()), constants.getConstantUtf8(nameAndType.getSignatureIndex()));
        if (methodTypes == null || !(methodTypes.getReturnedType() instanceof GenericType returned)
            || returned.getDimension() != 0 || declaresTypeParameter(methodTypes, returned.getName()))
        {
            return null;
        }
        return receiverType(invoke) instanceof ObjectType receiverType && owner.equals(receiverType.getInternalName())
            && receiverType.getTypeArguments() != null
            ? typeArgument(owner, returned.getName(), receiverType.getTypeArguments()) : null;
    }

    @Override
    protected Instruction match(Instruction instruction) {
        return null;
    }

    private boolean isRedundant(Instruction parent, CheckCast cast, InvokeNoStaticInstruction invoke)
    {
        ConstantPool constants = classFile.getConstantPool();
        ConstantCP methodref = constants.getConstantMethodref(invoke.getIndex());
        ConstantNameAndType nameAndType = constants.getConstantNameAndType(methodref.getNameAndTypeIndex());
        String owner = constants.getConstantClassName(methodref.getClassIndex());

        MethodTypes methodTypes = typeMaker.makeMethodTypes(owner,
            constants.getConstantUtf8(nameAndType.getNameIndex()),
            constants.getConstantUtf8(nameAndType.getSignatureIndex()));
        if (methodTypes == null || !(methodTypes.getReturnedType() instanceof GenericType returned)
            || returned.getDimension() != 0)
        {
            return false;
        }

        if (declaresTypeParameter(methodTypes, returned.getName())) {
            return false;
        }

        String receiverSignature = invoke.getObjectref().getReturnedSignature(classFile, localVariables);
        if (receiverSignature == null || receiverSignature.indexOf('<') == -1) {
            return false;
        }
        Type receiver = typeMaker.makeFromSignature(receiverSignature);
        if (!(receiver instanceof ObjectType receiverType) || receiverType.getDimension() != 0
            || !owner.equals(receiverType.getInternalName()) || receiverType.getTypeArguments() == null)
        {
            return false;
        }

        if (keepsRawCast(parent)) {
            return false;
        }

        TypeArgument argument = typeArgument(owner, returned.getName(), receiverType.getTypeArguments());
        return argument instanceof ObjectType argumentType && argumentType.getDimension() == 0
            && argumentType.getInternalName().equals(constants.getConstantClassName(cast.getIndex()))
            && (argumentType.getTypeArguments() == null || acceptsAnyArgument(parent));
    }

    /** The checked exceptions of a member declared with the type variables of the receiver depend on its type arguments. */
    private boolean throwsTypeVariable(Instruction parent)
    {
        if (!(parent instanceof InvokeNoStaticInstruction call)) {
            return false;
        }
        ConstantPool constants = classFile.getConstantPool();
        ConstantCP methodref = constants.getConstantMethodref(call.getIndex());
        ConstantNameAndType nameAndType = constants.getConstantNameAndType(methodref.getNameAndTypeIndex());
        MethodTypes types = typeMaker.makeMethodTypes(constants.getConstantClassName(methodref.getClassIndex()),
            constants.getConstantUtf8(nameAndType.getNameIndex()), constants.getConstantUtf8(nameAndType.getSignatureIndex()));
        if (types == null) {
            return true;
        }
        if (types.getExceptionTypes() != null) {
            for (Type thrown : types.getExceptionTypes()) {
                if (thrown instanceof GenericType) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Removing the raw cast would change the type of the selected member where it is used. */
    private boolean keepsRawCast(Instruction parent)
    {
        if (throwsTypeVariable(parent)) {
            return true;
        }
        Type selected = selectedType(parent);
        boolean dependent = selected == null || dependsOnTypeVariable(selected);
        if (parent instanceof PutField write && dependent && !assignsSameType(write, selected)) {
            return true;
        }
        // A raw receiver erases the generic types of the members it has: they are not the ones the value must fit
        if (isParameterized(selected) && (dependent || !fitsDestination(parent, parentOf.get(parent), selected))) {
            return true;
        }
        return dependent && (callArguments.contains(parent) ? callIsOverloaded(parent) : isTypeSensitive(parent, parentOf.get(parent)));
    }

    /** The value written to a field typed after the receiver has the type of the field as declared. */
    private boolean assignsSameType(PutField write, Type selected)
    {
        String signature = selected == null ? null : write.getValueref().getReturnedSignature(classFile, localVariables);
        // A type variable of the field is that of the receiver's class, not the value's
        if (signature == null || selected instanceof GenericType) {
            return false;
        }
        Type value = typeMaker.makeFromSignature(signature);
        // A raw value converts to any parameterization; the arguments of a call result are those of its own receiver
        return selected.equals(value)
            || value instanceof ObjectType other && selected instanceof ObjectType parameterized
                && other.getInternalName().equals(parameterized.getInternalName())
                && (other.getTypeArguments() == null || other.getTypeArguments() instanceof GenericType && isCalledOnSameType(write.getValueref(), selected));
    }

    /** The arguments of a call result are those of its receiver: they are the field's if the receiver has the type of the field. */
    private boolean isCalledOnSameType(Instruction value, Type selected)
    {
        if (!(value instanceof InvokeNoStaticInstruction call)) {
            return false;
        }
        String receiver = call.getObjectref().getReturnedSignature(classFile, localVariables);
        if (receiver == null) {
            return false;
        }
        Type type = typeMaker.makeFromSignature(receiver);
        return selected.equals(type)
            || type instanceof ObjectType other && selected instanceof ObjectType parameterized
                && other.getTypeArguments() == null && other.getInternalName().equals(parameterized.getInternalName());
    }

    /** A parameterized result is safe where the variable receiving it is declared with the very same type. */
    private boolean fitsDestination(Instruction parent, Instruction context, Type selected)
    {
        if (parent instanceof PutField write) {
            return assignsSameType(write, selected);
        }
        if (selected == null || !(context instanceof StoreInstruction store)) {
            return false;
        }
        LocalVariable lv = localVariables.getLocalVariableForStoreInstruction(store);
        return lv != null && lv.getSignatureIndex() > 0
            && selected.equals(typeMaker.makeFromSignature(lv.getSignature(classFile.getConstantPool())));
    }

    /** Operators and tests whose operand type must be compatible with the other operand or the tested type. */
    private static boolean isTypeSensitive(Instruction selected, Instruction context)
    {
        // A member selected from the value gets its own cast: the runtime check changes
        return context instanceof ObjectrefAttribute holder && holder.getObjectref() == selected
            || context instanceof ArrayInstruction || context instanceof ArrayStoreInstruction
            || context instanceof AThrow || context instanceof InstanceOf || context instanceof CheckCast || context instanceof BinaryOperatorInstruction
            || context instanceof IfCmp || context instanceof TernaryOperator || context instanceof Switch
            || context instanceof FastSynchronized || context instanceof InitArrayInstruction || context instanceof AssertInstruction
            || context instanceof FastSwitch;
    }

    /** Whether the call receiving the argument is generic or has an overload with as many parameters. */
    private boolean callIsOverloaded(Instruction argument)
    {
        return isGenericOrOverloaded(callOfArgument.get(argument));
    }

    private boolean isGenericOrOverloaded(InvokeInstruction call)
    {
        ConstantPool constants = classFile.getConstantPool();
        ConstantCP methodref = constants.getConstantMethodref(call.getIndex());
        ConstantNameAndType nameAndType = constants.getConstantNameAndType(methodref.getNameAndTypeIndex());
        MethodTypes called = typeMaker.makeMethodTypes(constants.getConstantClassName(methodref.getClassIndex()),
            constants.getConstantUtf8(nameAndType.getNameIndex()), constants.getConstantUtf8(nameAndType.getSignatureIndex()));
        String ownerName = constants.getConstantClassName(methodref.getClassIndex());
        if (called == null || called.getTypeParameters() != null
            || "java/lang/invoke/MethodHandle".equals(ownerName) || "java/lang/invoke/VarHandle".equals(ownerName)) {
            // The type arguments of a generic method are inferred from its arguments
            return true;
        }
        Set<String> erasures = new HashSet<>();
        collectOverloads(constants.getConstantClassName(methodref.getClassIndex()),
            constants.getConstantUtf8(nameAndType.getNameIndex()), call.getArgs().size(), new HashSet<>(), erasures);
        return erasures.size() > 1;
    }

    private void collectOverloads(String owner, String name, int parameters, Set<String> visited, Set<String> erasures)
    {
        if (owner == null || !visited.add(owner)) {
            return;
        }
        String entry = owner + StringConstants.CLASS_FILE_SUFFIX;
        try {
            byte[] bytes = loadClass(entry);
            if (bytes == null) {
                // Unknown owner: any overload may be declared there
                erasures.add("?");
                erasures.add("??");
                return;
            }
            JavaClass javaClass = new ClassParser(new ByteArrayInputStream(bytes), entry).parse();
            for (org.apache.bcel.classfile.Method method : javaClass.getMethods()) {
                if (method.getName().equals(name) && method.getArgumentTypes().length == parameters) {
                    erasures.add(method.getSignature());
                }
            }
            collectOverloads(toInternalName(javaClass.getSuperclassName()), name, parameters, visited, erasures);
            for (String implemented : javaClass.getInterfaceNames()) {
                collectOverloads(toInternalName(implemented), name, parameters, visited, erasures);
            }
        } catch (IOException e) {
            // Unknown: assume the worst
            erasures.add("?");
            erasures.add("??");
        }
    }

    /** The class comes from the loader of the decompiled classes, or else from the runtime that runs the decompiler. */
    private byte[] loadClass(String entry) throws IOException
    {
        Loader loader = classFile.getLoader();
        if (loader != null && loader.canLoad(entry)) {
            return loader.load(entry);
        }
        try (java.io.InputStream in = ClassLoader.getSystemResourceAsStream(entry)) {
            return in == null ? null : in.readAllBytes();
        }
    }

    private static String toInternalName(String className)
    {
        return className == null ? null : className.replace('.', '/');
    }

    /**
     * The selected member returns a type depending on the receiver: without the raw cast its static type
     * changes, and with it the overload chosen for the expression around.
     */
    private Type selectedType(Instruction parent)
    {
        ConstantPool constants = classFile.getConstantPool();
        Type selected;
        if (parent instanceof InvokeNoStaticInstruction call) {
            ConstantCP methodref = constants.getConstantMethodref(call.getIndex());
            ConstantNameAndType nameAndType = constants.getConstantNameAndType(methodref.getNameAndTypeIndex());
            MethodTypes types = typeMaker.makeMethodTypes(constants.getConstantClassName(methodref.getClassIndex()),
                constants.getConstantUtf8(nameAndType.getNameIndex()), constants.getConstantUtf8(nameAndType.getSignatureIndex()));
            selected = types == null ? null : types.getReturnedType();
        } else {
            GetField field = (GetField) parent;
            ConstantCP fieldref = constants.getConstantFieldref(field.getIndex());
            ConstantNameAndType nameAndType = constants.getConstantNameAndType(fieldref.getNameAndTypeIndex());
            selected = typeMaker.makeFieldType(constants.getConstantClassName(fieldref.getClassIndex()),
                constants.getConstantUtf8(nameAndType.getNameIndex()), constants.getConstantUtf8(nameAndType.getSignatureIndex()));
        }
        return selected;
    }

    /** A parameterized result must still fit the variable, field or parameter that receives it. */
    private static boolean isParameterized(Type selected)
    {
        return selected == null || selected instanceof ObjectType object && object.getTypeArguments() != null;
    }

    private static boolean dependsOnTypeVariable(TypeArgument type)
    {
        if (type instanceof GenericType) {
            return true;
        }
        if (type instanceof WildcardExtendsTypeArgument extendsBound) {
            return dependsOnTypeVariable(extendsBound.type());
        }
        if (type instanceof WildcardSuperTypeArgument superBound) {
            return dependsOnTypeVariable(superBound.type());
        }
        if (type instanceof ObjectType object && object.getTypeArguments() != null) {
            BaseTypeArgument arguments = object.getTypeArguments();
            if (!arguments.isTypeArgumentList()) {
                return dependsOnTypeVariable(arguments.getTypeArgumentFirst());
            }
            for (TypeArgument argument : arguments.getTypeArgumentList()) {
                if (dependsOnTypeVariable(argument)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** A type parameter of the method hides the one of the class with the same name. */
    private static boolean declaresTypeParameter(MethodTypes methodTypes, String name)
    {
        BaseTypeParameter parameters = methodTypes.getTypeParameters();
        if (parameters != null) {
            for (TypeParameter parameter : parameters) {
                if (parameter.getIdentifier().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** The raw cast of a parameterized type can be what makes the arguments of the call acceptable: it is kept unless they are null. */
    private boolean acceptsAnyArgument(Instruction parent)
    {
        return !(parent instanceof InvokeNoStaticInstruction call)
            || call.getArgs().stream().allMatch(AConstNull.class::isInstance) && !isGenericOrOverloaded(call);
    }

    private TypeArgument typeArgument(String owner, String typeParameterName, BaseTypeArgument arguments)
    {
        TypeTypes typeTypes = typeMaker.makeTypeTypes(owner);
        BaseTypeParameter typeParameters = typeTypes == null ? null : typeTypes.getTypeParameters();
        if (typeParameters == null) {
            return null;
        }
        int index = 0;
        for (TypeParameter typeParameter : typeParameters)
        {
            if (typeParameter.getIdentifier().equals(typeParameterName)) {
                break;
            }
            index++;
        }
        if (index >= arguments.typeArgumentSize()) {
            return null;
        }
        return arguments.isTypeArgumentList() ? arguments.getTypeArgumentList().get(index) : arguments.getTypeArgumentFirst();
    }
}
