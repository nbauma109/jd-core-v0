package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TryCatchFinallyTest extends AbstractTestCase {
    @Test
    public void testTryCatchFinally() throws Exception {
        String internalClassName = "jd/core/test/TryCatchFinally";
        String output = decompile(internalClassName);
        assertEquals(IOUtils.toString(getClass().getResource("TryCatchFinally.txt"), StandardCharsets.UTF_8), output);
    }
}
