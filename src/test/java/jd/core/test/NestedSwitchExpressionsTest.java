package jd.core.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

/** Switch expressions nested in the cases of another one used to be decompiled as statements losing the values. */
public class NestedSwitchExpressionsTest extends AbstractTestCase {

    @Test
    public void testJavac() throws IOException {
        assertDecompiles("/nested-switch-expressions-javac17.jar", "NestedSwitchExpressionsJavac17.txt");
    }

    @Test
    public void testEcj() throws IOException {
        assertDecompiles("/nested-switch-expressions-ecj17.jar", "NestedSwitchExpressionsEcj17.txt");
    }

    private void assertDecompiles(String jar, String expected) throws IOException {
        String name = "jd/core/test/fallbacks/NestedSwitchExpressions";
        try (InputStream in = getClass().getResourceAsStream(jar)) {
            String output = decompile(name, new ZipLoader(in), "17");
            assertEquals(IOUtils.toString(getClass().getResource(expected), StandardCharsets.UTF_8), output);
        }
    }
}
