package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class WaitTest extends AbstractTestCase {
    @Test
    public void testWait() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/wait-jdk17.0.4.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/Wait";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("Wait.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
