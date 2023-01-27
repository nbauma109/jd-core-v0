package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TryCatchSynchronizedTest extends AbstractTestCase {
    @Test
    public void testTryCatchSynchronized() throws Exception {
        String internalClassName = "jd/core/test/TryCatchSynchronized";
        String output = decompile(internalClassName);
        assertEquals(IOUtils.toString(getClass().getResource("TryCatchSynchronized.txt"), StandardCharsets.UTF_8), output);
    }
}
