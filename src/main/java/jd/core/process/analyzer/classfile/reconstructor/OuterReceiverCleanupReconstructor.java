package jd.core.process.analyzer.classfile.reconstructor;

import java.util.List;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.LocalVariable;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.Invokestatic;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;

/**
 * Completes the bytecode cleanup when the enclosing-instance access could not
 * be copied into the available space. Only a stable qualified this is copied.
 */
public final class OuterReceiverCleanupReconstructor {
    private OuterReceiverCleanupReconstructor() {
    }

    public static void reconstruct(ClassFile classFile, Method method, List<Instruction> instructions) {
        // Restrict the rewrite to a straight-line body with no other use of the local.
        if (instructions.size() != 2 || hasExceptionHandlers(method)
                || !(instructions.get(0) instanceof StoreInstruction store)
                || !(store.getValueref() instanceof GetStatic receiver)
                || receiver.getOpcode() != ByteCodeConstants.OUTERTHIS
                || !(instructions.get(1) instanceof ReturnInstruction returned)
                || !(returned.getValueref() instanceof Invokestatic call)) {
            return;
        }

        List<Instruction> arguments = call.getArgs();
        if (arguments.size() != 2 || !(arguments.get(0) instanceof ALoad first)
                || !(arguments.get(1) instanceof Invokevirtual secondCall)
                || !secondCall.getArgs().isEmpty()
                || !(secondCall.getObjectref() instanceof ALoad second)
                || store.getIndex() != first.getIndex() || first.getIndex() != second.getIndex()) {
            return;
        }

        LocalVariables variables = method.getLocalVariables();
        if (!isMatchingLocal(classFile, variables, receiver, first, second)) {
            return;
        }

        arguments.set(0, copy(receiver, first));
        secondCall.setObjectref(copy(receiver, second));
        instructions.remove(0);
        variables.removeLocalVariableWithIndexAndOffset(first.getIndex(), first.getOffset());
    }

    private static boolean hasExceptionHandlers(Method method) {
        return method.getCodeExceptions() != null && method.getCodeExceptions().length != 0;
    }

    private static boolean isMatchingLocal(ClassFile classFile, LocalVariables variables,
            GetStatic receiver, ALoad first, ALoad second) {
        if (variables == null) {
            return false;
        }
        LocalVariable local = variables.getLocalVariableWithIndexAndOffset(first.getIndex(), first.getOffset());
        return local != null && local.getStartPc() > 0
                && local == variables.getLocalVariableWithIndexAndOffset(second.getIndex(), second.getOffset())
                && local.getSignature(classFile.getConstantPool()).equals(receiver.getReturnedSignature(classFile, variables));
    }

    private static GetStatic copy(GetStatic receiver, ALoad load) {
        return new GetStatic(receiver.getOpcode(), load.getOffset(), load.getLineNumber(), receiver.getIndex());
    }
}
