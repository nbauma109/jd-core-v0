package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ForEachTest extends AbstractTestCase {

    @Test
    public void testJDK8() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/foreach-jdk1.8.0_331.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/ForEach", loader);
            assertEquals(IOUtils.toString(getClass().getResource("ForEachJDK8.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Test
    public void test() throws IOException {
        String output = decompile("jd/core/test/ForEach", "9");
        assertEquals(IOUtils.toString(getClass().getResource("ForEach.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void testFileAlterationObserver() throws IOException {
        String output = decompile("org/apache/commons/io/monitor/FileAlterationObserver");
        assertEquals(IOUtils.toString(getClass().getResource("FileAlterationObserver.txt"), StandardCharsets.UTF_8), output);
    }
}
