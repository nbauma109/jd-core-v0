package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TryResourcesTest extends AbstractTestCase {

    @Test
    public void test() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/jdk7-features.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/TryResources", loader);
            assertEquals(IOUtils.toString(getClass().getResource("TryResources.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Test
    public void testTryResources2() throws Exception {
        assertDecompiledEqualsForJars(
                new String[]{
                        "/try-resources-2-ecj-8.jar",
                        "/try-resources-2-jdk-1.8.0_331.jar",
                        "/try-resources-2-ecj-17.jar",
                        "/try-resources-2-jdk-17.0.11.jar"
                },
                "org/jd/core/v1/TryResources2",
                "TryResources2.txt"
        );
    }

    @Test
    public void testTryResourcesGeneric() throws Exception {
        assertDecompiledEquals(
                "/try-resources-generic-jdk-1.8.0_331.jar",
                "org/jd/core/v1/TryResources",
                "TryResourcesGeneric.txt"
        );
    }

    @Test
    public void testTryResourcesNewPattern() throws Exception {
        assertDecompiledEqualsForJars(
                new String[]{
                        "/try-resources-new-pattern-ecj-17.jar",
                        "/try-resources-new-pattern-jdk-17.0.11.jar"
                },
                "org/jd/core/v1/TryResourcesNewPattern",
                "TryResourcesNewPattern.txt"
        );
    }

    private void assertDecompiledEqualsForJars(String[] jarResourcePaths, String internalClassName, String expectedResourceName)
            throws Exception {
        for (String jarResourcePath : jarResourcePaths) {
            assertDecompiledEquals(jarResourcePath, internalClassName, expectedResourceName);
        }
    }

    private void assertDecompiledEquals(String jarResourcePath, String internalClassName, String expectedResourceName)
            throws Exception {
        URL expectedUrl = getClass().getResource(expectedResourceName);
        assertNotNull("Missing expected resource: " + expectedResourceName, expectedUrl);

        try (InputStream in = getClass().getResourceAsStream(jarResourcePath)) {
            assertNotNull("Missing jar resource: " + jarResourcePath, in);

            ZipLoader loader = new ZipLoader(in);
            String output = decompile(internalClassName, loader);
            String expected = IOUtils.toString(expectedUrl, StandardCharsets.UTF_8);

            assertEquals(expected, output);
        }
    }

    @Override
    protected boolean recompile() {
        return false;
    }
}
