package jd.core.test;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class JavaClassTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
		try (InputStream in = getClass().getResourceAsStream("/bcel-6.7.0.jar")) {
			CompositeLoader loader = new CompositeLoader(in);
	        String output = decompile("org/apache/bcel/classfile/JavaClass", loader);
	        assertEquals(IOUtils.toString(getClass().getResource("JavaClass.txt"), StandardCharsets.UTF_8), output);
		}
    }

    @Override
    protected boolean recompile() {
    	return false;
    }
}
