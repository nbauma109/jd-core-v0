package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TryCatchFinallyInTryCatchFinallyTest extends AbstractTestCase {

    @Test
    public void test() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/finally-in-catch-jikes-1.22.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/TryCatchFinallyInTryCatchFinally", loader);
            assertEquals(IOUtils.toString(getClass().getResource("TryCatchFinallyInTryCatchFinally.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
