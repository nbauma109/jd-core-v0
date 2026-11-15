package jd.core.test;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ExplicitRuntimeCheckCastTest extends AbstractTestCase {

    @Override
    protected boolean recompile() {
        // The unchecked cast to the type variable leaves nothing in the bytecode: the output has only the runtime check
        return false;
    }

    @Test
    public void explicitRuntimeCheckIsKept() throws Exception {
        String output = decompile("jd/core/test/ExplicitRuntimeCheckCast");
        assertTrue(output, output.contains("sink.put((") && output.contains("String"));
    }
}
