package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class Pass1VerifierTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/bcel/verifier/statics/Pass1Verifier");
        assertEquals(IOUtils.toString(getClass().getResource("Pass1Verifier.txt"), StandardCharsets.UTF_8), output);
    }
}
