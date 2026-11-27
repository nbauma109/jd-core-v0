package jd.core.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

/** A switch expression used as an argument or an operand used to be lost, silently, with the values before it. */
public class SwitchExpressionOperandsTest extends AbstractTestCase {

    @Test
    public void testJavac() throws IOException {
        assertDecompiles("/switch-expression-operands-javac17.jar", "SwitchExpressionOperandsJavac17.txt");
    }

    @Test
    public void testEcj() throws IOException {
        assertDecompiles("/switch-expression-operands-ecj17.jar", "SwitchExpressionOperandsEcj17.txt");
    }

    private void assertDecompiles(String jar, String expected) throws IOException {
        String name = "jd/core/test/fallbacks/SwitchExpressionOperands";
        try (InputStream in = getClass().getResourceAsStream(jar)) {
            String output = decompile(name, new ZipLoader(in), "17");
            assertEquals(IOUtils.toString(getClass().getResource(expected), StandardCharsets.UTF_8), output);
        }
    }
}
