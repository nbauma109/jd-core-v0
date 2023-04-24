package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class PathUtilsTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/io/file/PathUtils");
        assertEquals(IOUtils.toString(getClass().getResource("PathUtils.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void testJDK7() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/try-resources-jdk1.7.0_80.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/PathUtils", loader);
            assertEquals(IOUtils.toString(getClass().getResource("PathUtilsJDK7.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Override
    protected boolean recompile() {
        return false;
    }
}
