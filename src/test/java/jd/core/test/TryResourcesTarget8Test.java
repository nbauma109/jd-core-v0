package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * Try-with-resources compiled by javac 11 or later for a Java 8 target: the class file version
 * is below 11, but the code has the shape of the recent compilers (no primary exception variable).
 */
public class TryResourcesTarget8Test extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/try-resources-javac11-target8.jar")) {
            String output = decompile("jd/core/test/TryResourcesModern", new CompositeLoader(in));
            assertEquals(IOUtils.toString(getClass().getResource("TryResourcesTarget8.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
