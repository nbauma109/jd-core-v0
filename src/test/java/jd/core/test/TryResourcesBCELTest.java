package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TryResourcesBCELTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/try-resources-bcel-jdk1.8.0_331.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/TryResourcesBCEL", loader);
            assertEquals(IOUtils.toString(getClass().getResource("TryResourcesBCEL.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
