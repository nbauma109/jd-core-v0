package jd.core.test;

import java.util.List;

import org.apache.bcel.Const;
import org.junit.Test;

import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.AssignmentInstruction;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;
import jd.core.model.instruction.bytecode.instruction.StoreInstruction;
import jd.core.process.layouter.visitor.MinLineNumberVisitor;

import static org.junit.Assert.assertEquals;

public class MinLineNumberVisitorTest {
    @Test
    public void usesKnownInvocationLineWhenReceiverHasNoLine() {
        ALoad receiver = new ALoad(Const.ALOAD, 0, 0, 0);
        Invokevirtual invocation = new Invokevirtual(Const.INVOKEVIRTUAL, 2, 14, 0,
                receiver, List.of(new IConst(Const.ICONST_1, 1, 14, 1)));

        assertEquals(14, MinLineNumberVisitor.visit(invocation));
    }

    @Test
    public void usesKnownAssignmentLineWhenTargetHasNoLine() {
        ALoad target = new ALoad(Const.ALOAD, 0, 0, 0);
        IConst value = new IConst(Const.ICONST_1, 1, 22, 1);
        AssignmentInstruction assignment = new AssignmentInstruction(
                ByteCodeConstants.ASSIGNMENT, 2, 22, 14, "=", target, value);

        assertEquals(22, MinLineNumberVisitor.visit(assignment));
    }

    @Test
    public void assignmentStartsBeforeTheClosingCallLine() {
        ALoad receiver = new ALoad(Const.ALOAD, 0, 30, 0);
        Invokevirtual invocation = new Invokevirtual(Const.INVOKEVIRTUAL, 1, 33, 0, receiver, List.of());
        StoreInstruction assignment = new StoreInstruction(Const.ASTORE, 2, 33, 1,
                "Ljava/lang/Object;", invocation);

        assertEquals(30, MinLineNumberVisitor.visit(assignment));
    }

    @Test
    public void keepsEarlierKnownReceiverLine() {
        ALoad receiver = new ALoad(Const.ALOAD, 0, 13, 0);
        Invokevirtual invocation = new Invokevirtual(Const.INVOKEVIRTUAL, 1, 14, 0,
                receiver, List.of());

        assertEquals(13, MinLineNumberVisitor.visit(invocation));
    }
}
