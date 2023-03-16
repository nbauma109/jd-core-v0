package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ContinueInCatchTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/continue-in-catch-jdk1.8.0_331.jar")) {
            Loader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/ContinueInCatch", loader);
            assertEquals(IOUtils.toString(getClass().getResource("ContinueInCatch.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
