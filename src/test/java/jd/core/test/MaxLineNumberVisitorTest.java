package jd.core.test;

import java.util.List;

import org.apache.bcel.Const;
import org.junit.Test;

import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;
import jd.core.process.layouter.visitor.MaxLineNumberVisitor;

import static org.junit.Assert.assertEquals;

public class MaxLineNumberVisitorTest {
    @Test
    public void includesMultilineReceiverWhenInvocationHasArguments() {
        ALoad receiver = new ALoad(Const.ALOAD, 0, 14, 0);
        IConst argument = new IConst(Const.ICONST_1, 1, 12, 1);
        Invokevirtual invocation = new Invokevirtual(Const.INVOKEVIRTUAL, 2, 13, 0,
                receiver, List.of(argument));

        assertEquals(14, MaxLineNumberVisitor.visit(invocation));
    }
    @Test
    public void excludesCopiedFinallyLinesBeyondRetainedRange() {
        ALoad receiver = new ALoad(Const.ALOAD, 0, 329, 0);
        IConst argument = new IConst(Const.ICONST_1, 1, 332, 1);
        Invokevirtual invocation = new Invokevirtual(Const.INVOKEVIRTUAL, 2, 332, 0,
                receiver, List.of(argument));

        assertEquals(332, MaxLineNumberVisitor.visit(invocation));
        assertEquals(329, MaxLineNumberVisitor.visit(invocation, 331));
    }
}
