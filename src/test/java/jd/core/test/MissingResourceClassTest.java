package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The class of the resource cannot be loaded: it is supposed to be an AutoCloseable, as the compiler checked. The jar
 * has been compiled by javac 17 for Java 8 with these two classes, and contains only the first one:
 * <pre>
 * public class MissingResourceUser {
 *     static int read(MissingResource resource) throws Exception {
 *         try (MissingResource in = resource) { return in.value(); }
 *     }
 * }
 * public class MissingResource implements AutoCloseable { int value() { return 1; } public void close() { } }
 * </pre>
 */
public class MissingResourceClassTest extends AbstractTestCase {

    @Override
    protected boolean recompile() {
        // The class of the resource is missing
        return false;
    }

    @Test
    public void test() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/missing-resource-class-javac-target8.jar")) {
            String output = decompile("jd/core/test/MissingResourceUser", new CompositeLoader(in));
            assertTrue(output, output.contains("try (MissingResource in = resource)"));
            assertEquals(IOUtils.toString(getClass().getResource("MissingResourceUser.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
