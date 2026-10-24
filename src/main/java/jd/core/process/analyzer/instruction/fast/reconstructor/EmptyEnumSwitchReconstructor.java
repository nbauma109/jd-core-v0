package jd.core.process.analyzer.instruction.fast.reconstructor;

import java.util.List;
import org.apache.bcel.classfile.ConstantCP;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantNameAndType;
import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;
import jd.core.model.instruction.bytecode.instruction.Pop;
import jd.core.model.instruction.fast.FastConstants;
import jd.core.model.instruction.fast.instruction.FastSwitch;

/** Restore javac's empty enum switches after control-flow reconstruction. */
public final class EmptyEnumSwitchReconstructor {
    private EmptyEnumSwitchReconstructor() {
    }

    public static void visit(ClassFile classFile, List<Instruction> instructions) {
        for (int index = 0; index < instructions.size(); index++) {
            Instruction instruction = instructions.get(index);
            if (instruction instanceof Pop pop) {
                instructions.set(index, reconstruct(classFile, pop));
            } else {
                for (List<Instruction> block : SwitchExpressionReconstructor.getBlocks(instruction)) {
                    visit(classFile, block);
                }
            }
        }
    }

    private static Instruction reconstruct(ClassFile classFile, Pop pop) {
        if (!(pop.getObjectref() instanceof ArrayLoadInstruction load)
                || !(load.getArrayref() instanceof GetStatic table)
                || !(load.getIndexref() instanceof Invokevirtual ordinal) || !ordinal.getArgs().isEmpty()) {
            return pop;
        }
        ConstantPool constants = classFile.getConstantPool();
        ConstantFieldref field = constants.getConstantFieldref(table.getIndex());
        ConstantNameAndType fieldName = constants.getConstantNameAndType(field.getNameAndTypeIndex());
        ConstantCP method = constants.getConstantMethodref(ordinal.getIndex());
        ConstantNameAndType methodName = constants.getConstantNameAndType(method.getNameAndTypeIndex());
        if (classFile.getSwitchMapOwner(constants.getConstantUtf8(fieldName.getNameIndex())) != null
                && "ordinal".equals(constants.getConstantUtf8(methodName.getNameIndex()))
                && "()I".equals(constants.getConstantUtf8(methodName.getSignatureIndex()))) {
            // Keep switch-table initialization before evaluation of the selector.
            return new FastSwitch(FastConstants.SWITCH_ENUM, pop.getOffset(), pop.getLineNumber(),
                    0, load, new FastSwitch.Pair[0]);
        }
        return pop;
    }

}
