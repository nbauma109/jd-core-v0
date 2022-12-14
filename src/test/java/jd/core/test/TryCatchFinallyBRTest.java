package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TryCatchFinallyBRTest extends AbstractTestCase {
    @Test
    public void testJikes() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/try-catch-finally-jikes-1.22.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/TryCatchFinallyBR";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("TryCatchFinallyBR.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Test
    public void testJavac() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/try-catch-finally-javac-17.0.4.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/TryCatchFinallyBR";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("TryCatchFinallyBR.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
