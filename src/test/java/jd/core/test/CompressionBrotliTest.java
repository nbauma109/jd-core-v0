package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class CompressionBrotliTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/try-resources-brotli-jdk1.8.0_331.jar")) {
            Loader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/CompressionBrotli", loader);
            assertEquals(IOUtils.toString(getClass().getResource("CompressionBrotli.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
