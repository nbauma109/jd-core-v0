package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ForTest extends AbstractTestCase {

    @Test
    public void test() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/for-jdk1.4.2_19.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/For", loader);
            assertEquals(IOUtils.toString(getClass().getResource("For.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
