package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Assume;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TryResourcesModernTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        // ecj generates another shape for try-with-resources
        Assume.assumeTrue("javac".equals(System.getProperty("test.compiler", "javac")));
        String output = decompile("jd/core/test/TryResourcesModern");
        assertEquals(IOUtils.toString(getClass().getResource("TryResourcesModern.txt"), StandardCharsets.UTF_8), output);
    }
}
