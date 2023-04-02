package jd.core.test;

import org.junit.Test;

import static org.junit.Assert.assertNull;

import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.ExceptionLoad;
import jd.core.model.instruction.bytecode.instruction.GetField;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Invokeinterface;
import jd.core.model.instruction.bytecode.instruction.Ldc;
import jd.core.model.instruction.bytecode.instruction.Ldc2W;
import jd.core.model.instruction.bytecode.instruction.MultiANewArray;
import jd.core.model.instruction.bytecode.instruction.New;
import jd.core.model.instruction.fast.instruction.FastDeclaration;

public class NullClassFileTest {

    @Test
    public void testReturnedSignature() throws Exception {
        assertNull(new ANewArray(0, 0, 0, 0, null).getReturnedSignature(null, null));
        assertNull(new CheckCast(0, 0, 0, 0, null).getReturnedSignature(null, null));
        assertNull(new ExceptionLoad(0, 0, 0, 0).getReturnedSignature(null, null));
        assertNull(new FastDeclaration(0, 0, null, null).getReturnedSignature(null, null));
        assertNull(new GetField(0, 0, 0, 0, null).getReturnedSignature(null, null));
        assertNull(new GetStatic(0, 0, 0, 0).getReturnedSignature(null, null));
        assertNull(new Invokeinterface(0, 0, 0, 0, null, null).getReturnedSignature(null, null));
        assertNull(new Ldc2W(0, 0, 0, 0).getReturnedSignature(null, null));
        assertNull(new Ldc(0, 0, 0, 0).getReturnedSignature(null, null));
        assertNull(new MultiANewArray(0, 0, 0, 0, null).getReturnedSignature(null, null));
        assertNull(new New(0, 0, 0, 0).getReturnedSignature(null, null));
    }
}
