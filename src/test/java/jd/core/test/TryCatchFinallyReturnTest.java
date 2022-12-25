package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TryCatchFinallyReturnTest extends AbstractTestCase {

    @Test
    public void testTryCatchFinallyClass() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/try-catch-finally-return-jdk1.2.2.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/TryCatchFinallyReturn";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("TryCatchFinallyReturn.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
