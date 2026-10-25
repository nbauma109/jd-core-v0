package jd.core.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

/** Decompiles classes compiled by javac 21, whatever the JDK running the tests. */
public class Java21ConstructsJarTest extends AbstractTestCase {

    @Override
    protected boolean recompile() {
        return false;
    }

    @Test
    public void patternsAndTryWithResourcesAreReconstructed() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Constructs", new ZipLoader(in));
            assertFalse(output, output.contains("Byte code"));
            assertFalse(output, output.contains("addSuppressed"));
            assertFalse(output, output.contains("MatchException"));
            assertTrue(output, output.contains("instanceof String s"));
            assertTrue(output, output.matches("(?s).*instanceof [\\w.]*Pair\\(.*"));
            assertTrue(output, output.matches("(?s).*case [\\w.]*Circle\\(.*"));
            assertTrue(output, output.contains(" when "));
            assertTrue(output, output.contains("case null"));
            assertTrue(output, output.contains("try ("));
        }
    }
}
