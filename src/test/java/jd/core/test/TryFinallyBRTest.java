package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TryFinallyBRTest extends AbstractTestCase {
    @Test
    public void testTryFinally() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/try-finally-javac-17.0.4.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/TryFinallyBR";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("TryFinallyBR.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
