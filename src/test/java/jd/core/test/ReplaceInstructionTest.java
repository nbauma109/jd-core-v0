package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ReplaceInstructionTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("jd/core/test/ReplaceInstruction");
        assertEquals(IOUtils.toString(getClass().getResource("ReplaceInstruction.txt"), StandardCharsets.UTF_8), output);
    }
}
