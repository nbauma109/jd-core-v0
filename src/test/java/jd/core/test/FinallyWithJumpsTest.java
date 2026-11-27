package jd.core.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

/** ECJ places the copy of the "finally" block of a completing "try" block after the handler, if the block jumps. */
public class FinallyWithJumpsTest extends AbstractTestCase {

    @Test
    public void testJavac() throws IOException {
        assertDecompiles("/finally-with-jumps-javac17.jar", "FinallyWithJumpsJavac17.txt");
    }

    @Test
    public void testEcj() throws IOException {
        assertDecompiles("/finally-with-jumps-ecj17.jar", "FinallyWithJumpsEcj17.txt");
    }

    private void assertDecompiles(String jar, String expected) throws IOException {
        String name = "jd/core/test/fallbacks/FinallyWithJumps";
        try (InputStream in = getClass().getResourceAsStream(jar)) {
            String output = decompile(name, new ZipLoader(in), "17");
            assertRealignedLineNumbers(name, output);
            assertEquals(IOUtils.toString(getClass().getResource(expected), StandardCharsets.UTF_8), output);
        }
    }
}
