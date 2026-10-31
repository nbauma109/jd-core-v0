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

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.jd.core.v1.model.javasyntax.type.BaseTypeArgument;
import org.jd.core.v1.model.javasyntax.type.GenericType;
import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.model.javasyntax.type.Type;
import org.jd.core.v1.model.javasyntax.type.TypeArgument;
import org.jd.core.v1.model.javasyntax.type.WildcardExtendsTypeArgument;
import org.jd.core.v1.model.javasyntax.type.WildcardSuperTypeArgument;
import org.jd.core.v1.model.javasyntax.type.WildcardTypeArgument;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker.MethodTypes;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.process.analyzer.instruction.fast.visitor.AbstractReplaceInstructionVisitor;
import jd.core.util.SignatureUtil;

/**
 * An unchecked cast between two parameterizations of a type does not exist in the byte code:
 * {@code (Class<T>) inner.get()} with a {@code Class<? super T>} is an assignment of the
 * erased type. Adds the cast back where a value with wildcards is assigned to a type without.
 */
public class AddWildcardCastVisitor extends AbstractReplaceInstructionVisitor<Instruction>
{
    private final ClassFile classFile;
    private final Method method;
    private final LocalVariables localVariables;
    private final TypeMaker typeMaker;

    public AddWildcardCastVisitor(ClassFile classFile, Method method)
    {
        this.classFile = classFile;
        this.method = method;
        this.localVariables = method.getLocalVariables();
        this.typeMaker = new TypeMaker(classFile.getLoader());
    }

    @Override
    protected Instruction newInstruction(Instruction i, Instruction item) {
        return item;
    }

    @Override
    protected Instruction match(Instruction instruction) {
        return null;
    }

    @Override
    protected Instruction match(Instruction parentFound, Instruction instruction)
    {
        if (!(instruction instanceof InvokeInstruction invoke) || instruction instanceof CheckCast || localVariables == null) {
            return null;
        }
        String target = null;
        if (parentFound instanceof ReturnInstruction) {
            target = returnedSignature();
        } else if (parentFound instanceof StoreInstruction store) {
            LocalVariable lv = localVariables.getLocalVariableForStoreInstruction(store);
            target = lv == null || lv.getSignatureIndex() <= 0 ? null : lv.getSignature(classFile.getConstantPool());
        }
        if (target == null || target.indexOf('<') == -1 || !hasWildcards(invoke) || hasWildcards(typeMaker.makeFromSignature(target))) {
            return null;
        }
        Type targetType = typeMaker.makeFromSignature(target);
        if (!(targetType instanceof ObjectType targetObject) || targetObject.getTypeArguments() == null
            || !returnsClass(invoke, targetObject.getInternalName())) {
            return null;
        }
        ConstantPool constants = classFile.getConstantPool();
        Instruction value = invoke;
        if (!isDirectCastLegal(invoke, targetObject))
        {
            // (List<String>) (List<?>) numbers(): the cast goes through the unbounded wildcard
            String unbounded = "[".repeat(targetObject.getDimension()) + 'L' + targetObject.getInternalName() + '<'
                + "*".repeat(targetObject.getTypeArguments().typeArgumentSize()) + ">;";
            value = new CheckCast(Const.CHECKCAST, invoke.getOffset(), invoke.getLineNumber(),
                constants.addConstantUtf8(unbounded), invoke);
        }
        return new CheckCast(Const.CHECKCAST, invoke.getOffset(), invoke.getLineNumber(),
            constants.addConstantUtf8(target), value);
    }

    /** A cast between two parameterizations is legal unless some argument of the value can never be the one of the target. */
    private boolean isDirectCastLegal(InvokeInstruction invoke, ObjectType target)
    {
        ConstantPool constants = classFile.getConstantPool();
        ConstantCP methodref = constants.getConstantMethodref(invoke.getIndex());
        ConstantNameAndType nameAndType = constants.getConstantNameAndType(methodref.getNameAndTypeIndex());
        MethodTypes methodTypes = typeMaker.makeMethodTypes(constants.getConstantClassName(methodref.getClassIndex()),
            constants.getConstantUtf8(nameAndType.getNameIndex()),
            constants.getConstantUtf8(nameAndType.getSignatureIndex()));
        if (methodTypes == null || !(methodTypes.getReturnedType() instanceof ObjectType returned)
            || returned.getTypeArguments() == null
            || returned.getTypeArguments().typeArgumentSize() != target.getTypeArguments().typeArgumentSize())
        {
            return false;
        }
        for (int i = 0; i < target.getTypeArguments().typeArgumentSize(); i++)
        {
            if (!isCompatible(argumentAt(returned.getTypeArguments(), i), argumentAt(target.getTypeArguments(), i))) {
                return false;
            }
        }
        return true;
    }

    private static TypeArgument argumentAt(BaseTypeArgument arguments, int index)
    {
        return arguments.isTypeArgumentList() ? arguments.getTypeArgumentList().get(index) : arguments.getTypeArgumentFirst();
    }

    private boolean isCompatible(TypeArgument value, TypeArgument target)
    {
        if (value.equals(target) || value instanceof WildcardTypeArgument) {
            return true;
        }
        if (target instanceof GenericType) {
            // The bounds of the variable are unknown: a bounded wildcard of a concrete type needs the cast through '?'
            return !isBoundedByConcreteType(value);
        }
        if (target instanceof ObjectType targetType) {
            // The capture of '? extends B' is a subtype of B, the one of '? super B' a supertype of B
            if (value instanceof WildcardExtendsTypeArgument extendsBound && extendsBound.type() instanceof ObjectType bound) {
                return typeMaker.isAssignable(bound, targetType);
            }
            if (value instanceof WildcardSuperTypeArgument superBound && superBound.type() instanceof ObjectType bound) {
                return typeMaker.isAssignable(targetType, bound);
            }
        }
        // A wildcard bounded by a type variable: its bounds are unknown, the cast goes through '?'
        return false;
    }

    private static boolean isBoundedByConcreteType(TypeArgument value)
    {
        return value instanceof WildcardExtendsTypeArgument extendsBound && !(extendsBound.type() instanceof GenericType)
            || value instanceof WildcardSuperTypeArgument superBound && !(superBound.type() instanceof GenericType);
    }

    private String returnedSignature()
    {
        ConstantPool constants = classFile.getConstantPool();
        var attribute = method.getAttributeSignature();
        String signature = attribute == null ? null : constants.getConstantUtf8(attribute.getSignatureIndex());
        return signature == null ? null : SignatureUtil.getMethodReturnedSignature(signature);
    }

    /** Only a cast between two parameterizations of the same class is certain to be accepted. */
    private boolean returnsClass(InvokeInstruction invoke, String internalName)
    {
        ConstantPool constants = classFile.getConstantPool();
        ConstantCP methodref = constants.getConstantMethodref(invoke.getIndex());
        ConstantNameAndType nameAndType = constants.getConstantNameAndType(methodref.getNameAndTypeIndex());
        MethodTypes methodTypes = typeMaker.makeMethodTypes(constants.getConstantClassName(methodref.getClassIndex()),
            constants.getConstantUtf8(nameAndType.getNameIndex()),
            constants.getConstantUtf8(nameAndType.getSignatureIndex()));
        return methodTypes != null && methodTypes.getReturnedType() instanceof ObjectType returned
            && returned.getInternalName().equals(internalName);
    }

    private boolean hasWildcards(InvokeInstruction invoke)
    {
        ConstantPool constants = classFile.getConstantPool();
        ConstantCP methodref = constants.getConstantMethodref(invoke.getIndex());
        ConstantNameAndType nameAndType = constants.getConstantNameAndType(methodref.getNameAndTypeIndex());
        MethodTypes methodTypes = typeMaker.makeMethodTypes(constants.getConstantClassName(methodref.getClassIndex()),
            constants.getConstantUtf8(nameAndType.getNameIndex()),
            constants.getConstantUtf8(nameAndType.getSignatureIndex()));
        return methodTypes != null && hasWildcards(methodTypes.getReturnedType());
    }

    private static boolean hasWildcards(Type type)
    {
        if (!(type instanceof ObjectType objectType) || objectType.getTypeArguments() == null) {
            return false;
        }
        BaseTypeArgument arguments = objectType.getTypeArguments();
        for (int i = 0; i < arguments.typeArgumentSize(); i++) {
            TypeArgument argument = arguments.isTypeArgumentList() ? arguments.getTypeArgumentList().get(i) : arguments.getTypeArgumentFirst();
            if (argument instanceof WildcardExtendsTypeArgument || argument instanceof WildcardSuperTypeArgument
                || argument instanceof WildcardTypeArgument) {
                return true;
            }
        }
        return false;
    }
}
