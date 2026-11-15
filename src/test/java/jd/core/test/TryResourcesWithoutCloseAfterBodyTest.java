package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Assume;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/** The shapes are those of javac: the ecj shape of a body that never completes normally is not reconstructed. */
public class TryResourcesWithoutCloseAfterBodyTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        Assume.assumeTrue("javac".equals(System.getProperty("test.compiler", "javac")));
        String output = decompile("jd/core/test/TryResourcesWithoutCloseAfterBody");
        assertEquals(IOUtils.toString(getClass().getResource("TryResourcesWithoutCloseAfterBody.txt"), StandardCharsets.UTF_8), output);
    }
}
