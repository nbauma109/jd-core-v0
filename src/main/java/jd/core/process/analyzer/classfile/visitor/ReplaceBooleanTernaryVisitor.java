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

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.AssignmentInstruction;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.LoadInstruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.process.analyzer.instruction.bytecode.ComparisonInstructionAnalyzer;
import jd.core.process.analyzer.instruction.fast.visitor.AbstractReplaceInstructionVisitor;

/**
 * A boolean variable assigned in the middle of an expression is compiled as
 * {@code x = cond ? 1 : 0}. Replaces the ternary operator by its condition.
 */
public class ReplaceBooleanTernaryVisitor extends AbstractReplaceInstructionVisitor<Instruction>
{
    private final ClassFile classFile;
    private final LocalVariables localVariables;

    public ReplaceBooleanTernaryVisitor(ClassFile classFile, LocalVariables localVariables)
    {
        this.classFile = classFile;
        this.localVariables = localVariables;
    }

    @Override
    protected Instruction newInstruction(Instruction i, Instruction item) {
        return item;
    }

    @Override
    protected Instruction match(Instruction parentFound, Instruction instruction)
    {
        if (localVariables != null && instruction instanceof TernaryOperator ternary
            && ternary.getValue1() instanceof IConst first && ternary.getValue2() instanceof IConst second
            && first.getValue() + second.getValue() == 1 && isBooleanAssignment(parentFound))
        {
            if (first.getValue() == 0) {
                ComparisonInstructionAnalyzer.inverseComparison(ternary.getTest());
            }
            return ternary.getTest();
        }
        return null;
    }

    @Override
    protected Instruction match(Instruction instruction) {
        return null;
    }

    private boolean isBooleanAssignment(Instruction parent)
    {
        Instruction target = parent instanceof AssignmentInstruction assignment ? assignment.getValue1() : parent;
        LocalVariable lv = null;
        if (target instanceof StoreInstruction store) {
            lv = localVariables.getLocalVariableForStoreInstruction(store);
        } else if (target instanceof LoadInstruction load) {
            lv = localVariables.getLocalVariableWithIndexAndOffset(load.getIndex(), load.getOffset());
        }
        return lv != null && lv.getSignatureIndex() > 0
            && "Z".equals(classFile.getConstantPool().getConstantUtf8(lv.getSignatureIndex()));
    }
}
