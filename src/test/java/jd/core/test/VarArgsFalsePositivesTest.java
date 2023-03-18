package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class VarArgsFalsePositivesTest extends AbstractTestCase {
    @Test
    public void testVarArgsFalsePositives() throws Exception {
        String internalClassName = "jd/core/test/VarArgsFalsePositives";
        String output = decompile(internalClassName);
        assertEquals(IOUtils.toString(getClass().getResource("VarArgsFalsePositives.txt"), StandardCharsets.UTF_8), output);
    }
}
