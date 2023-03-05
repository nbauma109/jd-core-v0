package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class LRUCacheTest extends AbstractTestCase {
    @Test
    public void testJDK8() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/lru-cache-jdk1.8.0_331.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/LRUCache", loader);
            assertEquals(IOUtils.toString(getClass().getResource("LRUCacheJDK8.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Test
    public void test() throws Exception {
        String output = decompile("jd/core/test/LRUCache");
        assertEquals(IOUtils.toString(getClass().getResource("LRUCache.txt"), StandardCharsets.UTF_8), output);
    }
}
