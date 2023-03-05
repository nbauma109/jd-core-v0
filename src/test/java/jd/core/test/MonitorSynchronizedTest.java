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

    @Test
    public void testJavac122() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/monitor-synchronized-jdk1.2.2.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/MonitorSynchronized";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("MonitorSynchronized.txt"), StandardCharsets.UTF_8), output);
        }
    }
    
    @Test
    public void testJavac131() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/monitor-synchronized-jdk1.3.1_28.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/MonitorSynchronized";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("MonitorSynchronized.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Test
    public void testJavac180() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/monitor-synchronized-jdk1.8.0_331.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/MonitorSynchronized";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("MonitorSynchronized180.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
