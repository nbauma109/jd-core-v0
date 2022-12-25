package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TryFinallyReturnThrowTest extends AbstractTestCase {

    @Test
    public void test122() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/try-finally-return-throw-jdk1.2.2.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/TryFinallyReturnThrow";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("TryFinallyReturnThrow.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Test
    public void test180() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/try-finally-return-throw-jdk1.8.0_331.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/TryFinallyReturnThrow";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("TryFinallyReturnThrow180.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
