package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class StringConcatTest extends AbstractTestCase {

    @Test
    public void testStringConcatECJ() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/string-concat-ecj21.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/StringConcat", loader);
            assertEquals(IOUtils.toString(getClass().getResource("StringConcat.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Test
    public void testStringConcat() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/string-concat-jdk21.0.8.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/StringConcat", loader);
            assertEquals(IOUtils.toString(getClass().getResource("StringConcat.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Test
    public void testStringConcatIndy() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/string-concat-indy-jdk21.0.8.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/StringConcat", loader);
            assertEquals(IOUtils.toString(getClass().getResource("StringConcat.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
