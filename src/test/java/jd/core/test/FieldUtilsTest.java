package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/*
 * Test for FastInstructionListBuilder.createBreakAndContinue().
 */
public class FieldUtilsTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/lang3/reflect/FieldUtils");
        assertEquals(IOUtils.toString(getClass().getResource("FieldUtils.txt"), StandardCharsets.UTF_8), output);
    }
}
