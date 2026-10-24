package jd.core.process.analyzer.instruction.fast.reconstructor;

import java.util.List;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantFieldref;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.AssertInstruction;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.instruction.fast.FastConstants;
import jd.core.model.instruction.fast.instruction.FastTestList;
import jd.core.process.analyzer.instruction.bytecode.ComparisonInstructionAnalyzer;
import jd.core.process.analyzer.instruction.bytecode.reconstructor.AssertInstructionReconstructor;

/** Recover assertions whose boolean expressions needed ternary reconstruction first. */
public final class StructuredAssertReconstructor {
    private final ClassFile classFile;

    public StructuredAssertReconstructor(ClassFile classFile) {
        this.classFile = classFile;
    }

    public void visit(List<Instruction> instructions) {
        for (int index = 0; index < instructions.size(); index++) {
            Instruction instruction = instructions.get(index);
            AssertInstruction assertion = match(instruction);
            if (assertion != null) {
                instructions.set(index, assertion);
            } else {
                for (List<Instruction> block : SwitchExpressionReconstructor.getBlocks(instruction)) {
                    visit(block);
                }
            }
        }
    }

    private AssertInstruction match(Instruction instruction) {
        if (!(instruction instanceof FastTestList guard) || guard.getOpcode() != FastConstants.IF_SIMPLE
                || !isAssertionGuard(guard.getTest()) || guard.getInstructions().size() != 1) {
            return null;
        }
        Instruction body = guard.getInstructions().get(0);
        if (body instanceof AThrow throwing && throwing.getValue() instanceof InvokeNew error
                && isAssertionError(error)) {
            IConst condition = new IConst(ByteCodeConstants.ICONST, instruction.getOffset(),
                    instruction.getLineNumber(), 0);
            condition.setSignature("Z");
            return createAssertion(instruction, condition, error);
        }
        if (!(body instanceof FastTestList failure) || failure.getOpcode() != FastConstants.IF_SIMPLE
                || failure.getInstructions().size() != 1
                || !(failure.getInstructions().get(0) instanceof AThrow throwing)
                || !(throwing.getValue() instanceof InvokeNew error) || !isAssertionError(error)) {
            return null;
        }
        Instruction condition = failure.getTest();
        ComparisonInstructionAnalyzer.inverseComparison(condition);
        return createAssertion(instruction, condition, error);
    }

    private boolean isAssertionGuard(Instruction test) {
        if (!(test instanceof IfInstruction condition) || condition.getCmp() != ByteCodeConstants.CMP_EQ
                || !(condition.getValue() instanceof GetStatic disabled)) {
            return false;
        }
        ConstantPool constants = classFile.getConstantPool();
        ConstantFieldref reference = constants.getConstantFieldref(disabled.getIndex());
        return AssertInstructionReconstructor.isAssertionField(classFile, reference);
    }

    private static AssertInstruction createAssertion(Instruction instruction, Instruction condition, InvokeNew error) {
        Instruction message = error.getArgs().isEmpty() ? null : error.getArgs().get(0);
        return new AssertInstruction(ByteCodeConstants.ASSERT, instruction.getOffset(),
                instruction.getLineNumber(), condition, message);
    }

    private boolean isAssertionError(InvokeNew instruction) {
        ConstantPool constants = classFile.getConstantPool();
        ConstantCP reference = constants.getConstantMethodref(instruction.getIndex());
        return "java/lang/AssertionError".equals(constants.getConstantClassName(reference.getClassIndex()));
    }

}
