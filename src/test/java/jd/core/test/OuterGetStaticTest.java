package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class OuterGetStaticTest extends AbstractTestCase {
    @Test
    public void testJDK142() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/outer-getstatic-jdk1.4.2_19.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/OuterGetStatic";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("OuterGetStatic.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Test
    public void test() throws Exception {
        String output = decompile("jd/core/test/OuterGetStatic");
        assertEquals(IOUtils.toString(getClass().getResource("OuterGetStatic.txt"), StandardCharsets.UTF_8), output);
    }

    @Override
    protected boolean showDefaultConstructor() {
        return true;
    }
}
