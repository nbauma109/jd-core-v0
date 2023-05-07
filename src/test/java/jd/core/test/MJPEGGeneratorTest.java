package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class MJPEGGeneratorTest extends AbstractTestCase {

    @Test
    public void test() throws Exception {
        String output = decompile("net/sourceforge/plantuml/mjpeg/MJPEGGenerator");
        assertEquals(IOUtils.toString(getClass().getResource("MJPEGGenerator.txt"), StandardCharsets.UTF_8), output);
    }
}
