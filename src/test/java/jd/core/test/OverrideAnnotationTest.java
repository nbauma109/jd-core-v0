package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class OverrideAnnotationTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("jd/core/test/OverrideAnnotation", "16");
        assertEquals(IOUtils.toString(getClass().getResource("OverrideAnnotation.txt"), StandardCharsets.UTF_8), output);
    }
}
