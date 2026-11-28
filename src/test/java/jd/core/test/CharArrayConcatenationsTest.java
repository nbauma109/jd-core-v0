package jd.core.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

/** Concatenating the content of a character array used to be decompiled as concatenating the array. */
public class CharArrayConcatenationsTest extends AbstractTestCase {

    @Test
    public void testJavac() throws IOException {
        assertDecompiles("/char-array-concatenations-javac17.jar", "CharArrayConcatenationsJavac17.txt");
    }

    @Test
    public void testEcj() throws IOException {
        assertDecompiles("/char-array-concatenations-ecj17.jar", "CharArrayConcatenationsEcj17.txt");
    }

    private void assertDecompiles(String jar, String expected) throws IOException {
        String name = "jd/core/test/fallbacks/CharArrayConcatenations";
        try (InputStream in = getClass().getResourceAsStream(jar)) {
            String output = decompile(name, new ZipLoader(in), "17");
            assertEquals(IOUtils.toString(getClass().getResource(expected), StandardCharsets.UTF_8), output);
        }
    }
}
