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

import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.jd.core.v1.model.javasyntax.type.BaseTypeArgument;
import org.jd.core.v1.model.javasyntax.type.BaseTypeParameter;
import org.jd.core.v1.model.javasyntax.type.GenericType;
import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.jd.core.v1.model.javasyntax.type.Type;
import org.jd.core.v1.model.javasyntax.type.TypeArgument;
import org.jd.core.v1.model.javasyntax.type.TypeParameter;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker.MethodTypes;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker.TypeTypes;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.GetField;
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

    public ReplaceRedundantCastVisitor(ClassFile classFile, LocalVariables localVariables)
    {
        this.classFile = classFile;
        this.localVariables = localVariables;
        this.typeMaker = new TypeMaker(classFile.getLoader());
    }

    @Override
    protected Instruction newInstruction(Instruction i, Instruction item) {
        return item;
    }

    @Override
    protected Instruction match(Instruction parentFound, Instruction instruction)
    {
        // A member of the result is selected: the compiler inserts the cast again. Elsewhere it may be a runtime check.
        if (localVariables != null && (parentFound instanceof InvokeNoStaticInstruction || parentFound instanceof GetField) && ((ObjectrefAttribute) parentFound).getObjectref() == instruction
            && instruction instanceof CheckCast cast
            && cast.getObjectref() instanceof InvokeNoStaticInstruction invoke && isRedundant(cast, invoke))
        {
            return invoke;
        }
        return null;
    }

    @Override
    protected Instruction match(Instruction instruction) {
        return null;
    }

    private boolean isRedundant(CheckCast cast, InvokeNoStaticInstruction invoke)
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

        TypeArgument argument = typeArgument(owner, returned.getName(), receiverType.getTypeArguments());
        return argument instanceof ObjectType argumentType && argumentType.getDimension() == 0
            && argumentType.getInternalName().equals(constants.getConstantClassName(cast.getIndex()));
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
