package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class SwitchTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/switch-jdk1.4.2_19.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/Switch", loader);
            assertEquals(IOUtils.toString(getClass().getResource("Switch.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
