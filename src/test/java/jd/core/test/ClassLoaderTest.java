package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ClassLoaderTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
		try (InputStream in = getClass().getResourceAsStream("/bcel-6.7.0.jar")) {
			ZipLoader loader = new ZipLoader(in);
	        String output = decompile("org/apache/bcel/util/ClassLoader", loader);
	        assertEquals(IOUtils.toString(getClass().getResource("ClassLoader.txt"), StandardCharsets.UTF_8), output);
		}
    }
}
