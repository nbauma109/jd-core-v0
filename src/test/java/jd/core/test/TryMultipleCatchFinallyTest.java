package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TryMultipleCatchFinallyTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/try-multiple-catch-finally-jdk1.2.2.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/TryMultipleCatchFinally", loader);
            assertEquals(IOUtils.toString(getClass().getResource("TryMultipleCatchFinally.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
