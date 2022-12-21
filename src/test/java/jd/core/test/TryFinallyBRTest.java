package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TryFinallyBRTest extends AbstractTestCase {
    @Test
    public void testTryFinallyJavac() throws Exception {
        test("/try-finally-javac-17.0.4.jar");
    }

    @Test
    public void testTryFinallyEcj1_1() throws Exception {
        test("/try-catch-finally-ecj-1.1.jar");
    }

    @Test
    public void testTryFinallyEcj1_2() throws Exception {
        test("/try-catch-finally-ecj-1.2.jar");
    }

    @Test
    public void testTryFinallyEcj1_3() throws Exception {
        test("/try-catch-finally-ecj-1.3.jar");
    }

    @Test
    public void testTryFinallyEcj1_4() throws Exception {
        test("/try-catch-finally-ecj-1.4.jar");
    }

    @Test
    public void testTryFinallyEcj17() throws Exception {
        String internalClassName = "jd/core/test/TryFinallyBR";
        String output = decompile(internalClassName);
        assertEquals(IOUtils.toString(getClass().getResource("TryFinallyBR.txt"), StandardCharsets.UTF_8), output);
    }

    private void test(String jarPath) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(jarPath)) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/TryFinallyBR";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("TryFinallyBR.txt"), StandardCharsets.UTF_8), output);
        }
    }

}
