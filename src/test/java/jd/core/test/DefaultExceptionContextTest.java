package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/*
 * Test for duplicate variable declaration bug fix.
 * Check for declaredNames in FastInstructionListBuilder.analyzeTryAndSynchronized().
 */
public class DefaultExceptionContextTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/lang3/exception/DefaultExceptionContext");
        assertEquals(IOUtils.toString(getClass().getResource("DefaultExceptionContext.txt"), StandardCharsets.UTF_8), output);
    }
}
