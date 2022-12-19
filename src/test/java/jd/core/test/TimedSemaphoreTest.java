package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/*
 * Test for FastInstructionListBuilder.createContinue().
 */
public class TimedSemaphoreTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/lang3/concurrent/TimedSemaphore");
        assertEquals(IOUtils.toString(getClass().getResource("TimedSemaphore.txt"), StandardCharsets.UTF_8), output);
    }
}
