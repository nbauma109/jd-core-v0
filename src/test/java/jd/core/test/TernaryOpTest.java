package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TernaryOpTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/ternary-op-jdk1.8.0_331.jar")) {
            String internalClassName = "jd/core/test/TernaryOp";
            Loader loader = new ZipLoader(in);
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("TernaryOp.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Test
    public void testPostInc() throws Exception {
        String internalClassName = "jd/core/test/TernaryOpPostInc";
        String output = decompile(internalClassName);
        assertEquals(IOUtils.toString(getClass().getResource("TernaryOpPostInc.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void testPreInc() throws Exception {
        String internalClassName = "jd/core/test/TernaryOpPreInc";
        String output = decompile(internalClassName);
        assertEquals(IOUtils.toString(getClass().getResource("TernaryOpPreInc.txt"), StandardCharsets.UTF_8), output);
    }
}
