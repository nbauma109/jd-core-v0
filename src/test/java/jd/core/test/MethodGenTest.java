package jd.core.test;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class MethodGenTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
		try (InputStream in = getClass().getResourceAsStream("/bcel-6.7.0.jar")) {
			CompositeLoader loader = new CompositeLoader(in);
	        String output = decompile("org/apache/bcel/generic/MethodGen", loader);
	        assertEquals(IOUtils.toString(getClass().getResource("MethodGen.txt"), StandardCharsets.UTF_8), output);
		}
    }
}
