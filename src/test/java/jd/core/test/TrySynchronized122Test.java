package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TrySynchronized122Test extends AbstractTestCase {

    @Test
    public void testTryCatchFinallyClass() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/try-synchronized-jdk1.2.2.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/TryCatchFinallyClassForTest";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("TryCatchFinallyClassForTest.txt"), StandardCharsets.UTF_8), output);
        }
    }
    
    @Test
    public void testSynchronize() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/try-synchronized-jdk1.2.2.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/TestSynchronize";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("TestSynchronize.txt"), StandardCharsets.UTF_8), output);
        }
    }
    
    @Test
    public void testEmptySynchronizedBlock() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/try-synchronized-jdk1.2.2.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/EmptySynchronizedBlock";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("EmptySynchronizedBlock.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
