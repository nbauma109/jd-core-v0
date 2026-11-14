package jd.core.test;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ArrayParameterCastTest extends AbstractTestCase {

    @Override
    protected boolean recompile() {
        // The cast is printed with the erased type of the array: the generic type cannot be recompiled
        return false;
    }

    @Test
    public void castToArrayParameterIsKept() throws Exception {
        String output = decompile("jd/core/test/ArrayParameterCast");
        assertTrue(output, output.contains("sink.set(("));
    }

    @Test
    public void explicitRuntimeCheckIsKept() throws Exception {
        String output = decompile("jd/core/test/ArrayParameterCast");
        assertTrue(output, output.contains("sink.put((") && output.contains("String"));
    }
}
