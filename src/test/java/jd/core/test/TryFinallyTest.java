package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TryFinallyTest extends AbstractTestCase {
    @Test
    public void testTryFinally() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/try-finally-javac-1.8.0_331.jar")) {
            String internalClassName = "jd/core/test/TryFinally";
            Loader loader = new ZipLoader(in);
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("TryFinally.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
