package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TryFinallyTest extends AbstractTestCase {

    @Test
    public void testTryFinally180() throws Exception {
        test("/try-finally-javac-1.8.0_331.jar", "TryFinally180.txt");
    }
    
    @Test
    public void testTryFinally142() throws Exception {
        test("/try-finally-javac-1.4.2_19.jar", "TryFinally142.txt");
    }

    @Test
    public void testTryFinally131() throws Exception {
        test("/try-finally-javac-1.3.1_28.jar", "TryFinally131.txt");
    }
    
    @Test
    public void testTryFinally122() throws Exception {
        test("/try-finally-javac-1.2.2.jar", "TryFinally122.txt");
    }

    private void test(String jarPath, String expectedResultFile) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(jarPath)) {
            String internalClassName = "jd/core/test/TryFinally";
            Loader loader = new ZipLoader(in);
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource(expectedResultFile), StandardCharsets.UTF_8), output);
        }
    }
}
