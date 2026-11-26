package jd.core.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

/** The "break" of a case ending with a try statement used to be lost, or written with a "continue" outside any loop. */
public class SwitchCaseEndingWithTryTest extends AbstractTestCase {

    @Test
    public void testJavac() throws Exception {
        assertDecompiles("/switch-case-try-javac17.jar", "SwitchCaseEndingWithTryJavac17.txt");
    }

    @Test
    public void testEcj() throws Exception {
        assertDecompiles("/switch-case-try-ecj17.jar", "SwitchCaseEndingWithTryEcj17.txt");
    }

    @Test
    public void testEcjFallingThrough() throws Exception {
        assertDecompiles("/switch-case-try-ecj17.jar", "SwitchCaseFallingThroughTry",
                "SwitchCaseFallingThroughTryEcj17.txt");
    }

    private void assertDecompiles(String jar, String expected) throws IOException {
        assertDecompiles(jar, "SwitchCaseEndingWithTry", expected);
    }

    private void assertDecompiles(String jar, String simpleName, String expected) throws IOException {
        String name = "jd/core/test/fallbacks/" + simpleName;
        try (InputStream in = getClass().getResourceAsStream(jar)) {
            String output = decompile(name, new ZipLoader(in), "17");
            assertRealignedLineNumbers(name, output);
            assertEquals(IOUtils.toString(getClass().getResource(expected), StandardCharsets.UTF_8), output);
        }
    }
}
