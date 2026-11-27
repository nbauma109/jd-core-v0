package jd.core.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

/** The "break" following a try statement which returns used to be written inside its "finally" block. */
public class SwitchCaseReturningTryTest extends AbstractTestCase {

    @Test
    public void testJavac() throws IOException {
        assertDecompiles("/switch-case-return-javac17.jar", "SwitchCaseReturningTryJavac17.txt");
    }

    @Test
    public void testEcj() throws IOException {
        assertDecompiles("/switch-case-return-ecj17.jar", "SwitchCaseReturningTryEcj17.txt");
    }

    private void assertDecompiles(String jar, String expected) throws IOException {
        String name = "jd/core/test/fallbacks/SwitchCaseReturningTry";
        try (InputStream in = getClass().getResourceAsStream(jar)) {
            String output = decompile(name, new ZipLoader(in), "17");
            assertRealignedLineNumbers(name, output);
            assertEquals(IOUtils.toString(getClass().getResource(expected), StandardCharsets.UTF_8), output);
        }
    }
}
