package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/*
 * Test for duplicate variable declaration bug fix.
 * Check for declaredNames in FastInstructionListBuilder.analyzeTryAndSynchronized().
 */
public class DefaultExceptionContextTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
    	try (InputStream in = getClass().getResourceAsStream("/commons-lang3-3.12.0.jar")) {
    		ZipLoader loader = new ZipLoader(in);
	        String output = decompile("org/apache/commons/lang3/exception/DefaultExceptionContext", loader);
	        assertEquals(IOUtils.toString(getClass().getResource("DefaultExceptionContext.txt"), StandardCharsets.UTF_8), output);
    	}
    }
}
