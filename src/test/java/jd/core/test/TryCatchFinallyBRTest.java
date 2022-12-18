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
        test("jikes-1.22", false);
    }
    @Test
    public void testJavac_1_2_2_novar() throws Exception {
        test("javac-1.2.2", true);
    }

    @Test
    public void testJavac_1_3_1_28_novar() throws Exception {
        test("javac-1.3.1_28", true);
    }
   
    @Test
    public void testJavac_1_4_2_19() throws Exception {
        test("javac-1.4.2_19", false);
    }

    @Test
    public void testJavac_17_0_4() throws Exception {
        test("javac-17.0.4", false);
    }

    private void test(String compiler, boolean noVar) throws IOException {
        StringBuilder expectedFileName = new StringBuilder("TryCatchFinallyBR");
        StringBuilder jarPath = new StringBuilder("/try-catch-finally-");
        jarPath.append(compiler);
        if (noVar) {
            expectedFileName.append("-novar");
            jarPath.append("-novar");
        }
        expectedFileName.append(".txt");
        jarPath.append(".jar");
        try (InputStream in = getClass().getResourceAsStream(jarPath.toString())) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/TryCatchFinallyBR";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource(expectedFileName.toString()), StandardCharsets.UTF_8), output);
        }
    }
}
