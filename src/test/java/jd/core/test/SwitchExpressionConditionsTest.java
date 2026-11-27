package jd.core.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

/** Conditions in the cases of a switch expression used to give references to variables which do not exist. */
public class SwitchExpressionConditionsTest extends AbstractTestCase {

    @Test
    public void testJavac() throws IOException {
        assertDecompiles("/switch-expression-conditions-javac17.jar", "SwitchExpressionConditionsJavac17.txt");
    }

    @Test
    public void testEcj() throws IOException {
        assertDecompiles("/switch-expression-conditions-ecj17.jar", "SwitchExpressionConditionsEcj17.txt");
    }

    private void assertDecompiles(String jar, String expected) throws IOException {
        String name = "jd/core/test/fallbacks/SwitchExpressionConditions";
        try (InputStream in = getClass().getResourceAsStream(jar)) {
            String output = decompile(name, new ZipLoader(in), "17");
            assertEquals(IOUtils.toString(getClass().getResource(expected), StandardCharsets.UTF_8), output);
        }
    }
}
