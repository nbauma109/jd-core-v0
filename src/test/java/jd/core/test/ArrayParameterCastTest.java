package jd.core.test;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ArrayParameterCastTest extends AbstractTestCase {

    @Test
    public void castToArrayParameterIsKept() throws Exception {
        String output = decompile("jd/core/test/ArrayParameterCast");
        assertTrue(output, output.contains("sink.set((R[])"));
    }
}
