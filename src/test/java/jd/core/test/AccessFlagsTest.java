package jd.core.test;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

public class AccessFlagsTest extends AbstractTestCase {
    @Test
	public void test() throws Exception {
		try (InputStream in = getClass().getResourceAsStream("/bcel-6.7.0.jar")) {
			ZipLoader loader = new ZipLoader(in);
			String output = decompile("org/apache/bcel/classfile/AccessFlags", loader);
			assertEquals(IOUtils.toString(getClass().getResource("AccessFlags.txt"), StandardCharsets.UTF_8), output);
		}
	}
}
