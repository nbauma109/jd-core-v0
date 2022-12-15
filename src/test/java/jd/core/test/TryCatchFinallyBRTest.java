package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TryCatchFinallyBRTest extends AbstractTestCase {

    @Test
    public void testJikes() throws Exception {
        test("jikes-1.22");
    }

    @Test
    public void testJavac_1_4_2_19() throws Exception {
        test("javac-1.4.2_19");
    }

    @Test
    public void testJavac_17_0_4() throws Exception {
        test("javac-17.0.4");
    }

    private void test(String compiler) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/try-catch-finally-" + compiler + ".jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/TryCatchFinallyBR";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("TryCatchFinallyBR.txt"), StandardCharsets.UTF_8), output);
        }
    }

}
