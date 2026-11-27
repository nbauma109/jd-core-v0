package jd.core.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

/** Operators nested in the false branch of another one used to be decompiled as nested in its condition. */
public class NestedTernariesTest extends AbstractTestCase {

    @Test
    public void testJavac() throws IOException {
        assertDecompiles("/nested-ternaries-javac17.jar", "NestedTernariesJavac17.txt");
    }

    @Test
    public void testEcj() throws IOException {
        assertDecompiles("/nested-ternaries-ecj17.jar", "NestedTernariesEcj17.txt");
    }

    private void assertDecompiles(String jar, String expected) throws IOException {
        String name = "jd/core/test/fallbacks/NestedTernaries";
        try (InputStream in = getClass().getResourceAsStream(jar)) {
            String output = decompile(name, new ZipLoader(in), "17");
            assertEquals(IOUtils.toString(getClass().getResource(expected), StandardCharsets.UTF_8), output);
        }
    }
}
