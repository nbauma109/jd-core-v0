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

import java.util.List;
import java.util.Set;

import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeInstruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.process.analyzer.instruction.fast.visitor.AbstractReplaceInstructionVisitor;
import jd.core.util.SignatureUtil;

/**
 * Removes the explicit boxing of an argument, {@code Integer.valueOf(0)}, when the parameter is a
 * reference type and the method has no overload with the same number of parameters that the
 * primitive value could select instead ({@code List.remove(int)} and {@code remove(Object)}).
 */
public class ReplaceBoxingVisitor extends AbstractReplaceInstructionVisitor<Instruction>
{
    private static final Set<String> BOXES = Set.of("java/lang/Integer", "java/lang/Long", "java/lang/Double", "java/lang/Float");

    private final ClassFile classFile;
    private final LocalVariables localVariables;
    private final TypeMaker typeMaker;

    public ReplaceBoxingVisitor(ClassFile classFile, LocalVariables localVariables)
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
    protected Instruction match(Instruction instruction) {
        return null;
    }

    @Override
    protected Instruction match(Instruction parentFound, Instruction instruction)
    {
        if (parentFound instanceof InvokeInstruction invoke && instruction instanceof Invokestatic boxing
            && boxing.getArgs().size() == 1 && isBoxing(boxing) && !isConversion(boxing.getArgs().get(0)) && hasBoxedType(boxing) && isReferenceParameter(invoke, boxing)
            && hasSingleCandidate(invoke))
        {
            return boxing.getArgs().get(0);
        }
        return null;
    }

    /** A byte, short or char argument of Integer.valueOf is widened without any instruction: the types must be the same. */
    private boolean hasBoxedType(Invokestatic boxing)
    {
        ConstantPool constants = classFile.getConstantPool();
        ConstantCP methodref = constants.getConstantMethodref(boxing.getIndex());
        String descriptor = constants.getConstantUtf8(constants.getConstantNameAndType(methodref.getNameAndTypeIndex()).getSignatureIndex());
        String argumentSignature = boxing.getArgs().get(0).getReturnedSignature(classFile, localVariables);
        return argumentSignature != null && argumentSignature.equals(descriptor.substring(1, 2));
    }

    /** A widening, {@code Long.valueOf(i)} with an int, would box another class once the call is removed. */
    private static boolean isConversion(Instruction argument)
    {
        return argument.getOpcode() == ByteCodeConstants.IMPLICITCONVERT || argument.getOpcode() == ByteCodeConstants.CONVERT;
    }

    private boolean isBoxing(Invokestatic invoke)
    {
        ConstantPool constants = classFile.getConstantPool();
        ConstantCP methodref = constants.getConstantMethodref(invoke.getIndex());
        ConstantNameAndType nameAndType = constants.getConstantNameAndType(methodref.getNameAndTypeIndex());
        String owner = constants.getConstantClassName(methodref.getClassIndex());
        String descriptor = constants.getConstantUtf8(nameAndType.getSignatureIndex());
        return BOXES.contains(owner) && "valueOf".equals(constants.getConstantUtf8(nameAndType.getNameIndex()))
            && descriptor.length() > 3 && descriptor.charAt(0) == '(' && descriptor.charAt(2) == ')'
            && descriptor.endsWith(")L" + owner + ';');
    }

    private boolean isReferenceParameter(InvokeInstruction invoke, Instruction argument)
    {
        int position = invoke.getArgs().indexOf(argument);
        List<String> parameters = SignatureUtil.getParameterSignatures(descriptor(invoke));
        return position >= 0 && position < parameters.size()
            && (parameters.get(position).charAt(0) == 'L' || parameters.get(position).charAt(0) == 'T');
    }

    private boolean hasSingleCandidate(InvokeInstruction invoke)
    {
        ConstantPool constants = classFile.getConstantPool();
        ConstantCP methodref = constants.getConstantMethodref(invoke.getIndex());
        ConstantNameAndType nameAndType = constants.getConstantNameAndType(methodref.getNameAndTypeIndex());
        String name = constants.getConstantUtf8(nameAndType.getNameIndex());
        return !"<init>".equals(name)
            && typeMaker.matchCount(constants.getConstantClassName(methodref.getClassIndex()), name, invoke.getArgs().size(), false) == 1;
    }

    private String descriptor(InvokeInstruction invoke)
    {
        ConstantPool constants = classFile.getConstantPool();
        ConstantCP methodref = constants.getConstantMethodref(invoke.getIndex());
        return constants.getConstantUtf8(constants.getConstantNameAndType(methodref.getNameAndTypeIndex()).getSignatureIndex());
    }
}
