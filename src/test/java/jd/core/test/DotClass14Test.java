package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class DotClass14Test extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/dotclass14-jdk1.4.2_19.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/DotClass14";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("DotClass14.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Test
    public void testEcj() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/dotclass14-ecj-1.4.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/DotClass14";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("DotClass14Ecj.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
