package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TryResourcesTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/jdk7-features.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/TryResources", loader);
            assertEquals(IOUtils.toString(getClass().getResource("TryResources.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
