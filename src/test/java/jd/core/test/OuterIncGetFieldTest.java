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
        try (InputStream in = getClass().getResourceAsStream("/outer-inc-getstatic-jdk1.8.0_331.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/OuterIncGetField";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("OuterIncGetField.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
