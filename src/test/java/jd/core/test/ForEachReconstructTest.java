package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ForEachReconstructTest extends AbstractTestCase {

    @Test
    public void test() throws IOException {
        String output = decompile("jd/core/test/ForEachReconstruct");
        assertEquals(IOUtils.toString(getClass().getResource("ForEachReconstruct.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void test180() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/foreach-jdk1.8.0_331.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/ForEachReconstruct", loader);
            assertEquals(IOUtils.toString(getClass().getResource("ForEachReconstruct.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
