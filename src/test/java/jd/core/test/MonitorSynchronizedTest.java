package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class MonitorSynchronizedTest extends AbstractTestCase {
    @Test
    public void testJikes() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/monitor-synchronized-jikes-1.22.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/MonitorSynchronized";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("MonitorSynchronized.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
