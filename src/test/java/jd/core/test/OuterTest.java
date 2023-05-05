package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class OuterTest extends AbstractTestCase {

    @Test
    public void test() throws Exception {
        String internalClassName = "jd/core/test/Outer";
        String output = decompile(internalClassName);
        assertEquals(IOUtils.toString(getClass().getResource("Outer.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void testJDK142() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/inner-class-j2sdk1.4.2_19.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/Outer";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("Outer142.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Test
    public void testMissingInnerClass() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/inner-class-missing-j2sdk1.4.2_19.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/Outer";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("Outer142MissingInnerClass.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
