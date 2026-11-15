package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Assume;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class SynchronizedInTryCatchFinallyTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        // The shape that ecj generates for a catch and a finally around a synchronized block is not reconstructed
        Assume.assumeTrue("javac".equals(System.getProperty("test.compiler", "javac")));
        String output = decompile("jd/core/test/SynchronizedInTryCatchFinally");
        assertEquals(IOUtils.toString(getClass().getResource("SynchronizedInTryCatchFinally.txt"), StandardCharsets.UTF_8), output);
    }
}
