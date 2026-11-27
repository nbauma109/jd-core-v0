package jd.core.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

/** A switch expression giving an element of an array initializer used to give code which does not compile. */
public class SwitchExpressionArraysTest extends AbstractTestCase {

    @Test
    public void testJavac() throws IOException {
        assertDecompiles("/switch-expression-arrays-javac17.jar", "SwitchExpressionArraysJavac17.txt");
    }

    @Test
    public void testEcj() throws IOException {
        assertDecompiles("/switch-expression-arrays-ecj17.jar", "SwitchExpressionArraysEcj17.txt");
    }

    private void assertDecompiles(String jar, String expected) throws IOException {
        String name = "jd/core/test/fallbacks/SwitchExpressionArrays";
        try (InputStream in = getClass().getResourceAsStream(jar)) {
            String output = decompile(name, new ZipLoader(in), "17");
            assertEquals(IOUtils.toString(getClass().getResource(expected), StandardCharsets.UTF_8), output);
        }
    }
}
