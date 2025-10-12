package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/*
 * Test for FastInstructionListBuilder.createContinue().
 */
public class TimedSemaphoreTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
    	try (InputStream in = getClass().getResourceAsStream("/commons-lang3-3.12.0.jar")) {
    		CompositeLoader loader = new CompositeLoader(in);
	        String output = decompile("org/apache/commons/lang3/concurrent/TimedSemaphore", loader);
	        assertEquals(IOUtils.toString(getClass().getResource("TimedSemaphore.txt"), StandardCharsets.UTF_8), output);
    	}
    }
}
