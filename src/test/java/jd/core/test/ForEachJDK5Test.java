package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class ForEachJDK5Test extends AbstractTestCase {

    @Test
    public void testSunJDK5() throws Exception {
        test("/foreach-jdk1.5.0_22.jar");
    }

    @Test
    public void testSunJDK6() throws Exception {
        test("/foreach-jdk1.6.0_45.jar");
    }

    private void test(String jarPath) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(jarPath)) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/ForEachJDK5", loader);
            assertEquals(IOUtils.toString(getClass().getResource("ForEachJDK5.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
