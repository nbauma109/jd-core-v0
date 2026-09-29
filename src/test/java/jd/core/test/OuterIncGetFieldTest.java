package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class OuterIncGetFieldTest extends AbstractTestCase {

    @Test
    public void testJDK180() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/outer-inc-getfield-jdk1.8.0_331.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/OuterIncGetField";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("OuterIncGetField.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Test
    public void testJDK21() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/outer-inc-getfield-jdk21.0.9.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/OuterIncGetField", loader);
            assertEquals(IOUtils.toString(getClass().getResource("OuterIncGetFieldJDK21.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
