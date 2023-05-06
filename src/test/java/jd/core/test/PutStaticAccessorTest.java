package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class PutStaticAccessorTest extends AbstractTestCase {

    @Test
    public void test180() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/putstatic-accessor-jdk1.8.0_331.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/PutStaticAccessor", loader, "9");
            assertEquals(IOUtils.toString(getClass().getResource("PutStaticAccessor.txt"), StandardCharsets.UTF_8), output);
        }
    }
    
    @Test
    public void test142() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/putstatic-accessor-jdk1.4.2_19.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/PutStaticAccessor", loader, "9");
            assertEquals(IOUtils.toString(getClass().getResource("PutStaticAccessor.txt"), StandardCharsets.UTF_8), output);
        }
    }
    
    @Test
    public void test122() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/putstatic-accessor-jdk1.2.2.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/PutStaticAccessor", loader, "9");
            assertEquals(IOUtils.toString(getClass().getResource("PutStaticAccessor.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Override
    protected boolean showLineNumbers() {
        return false;
    }
}
