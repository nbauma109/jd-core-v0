package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class AnnotationTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("jd/core/test/TestAnnotation");
        assertEquals(IOUtils.toString(getClass().getResource("TestAnnotation.txt"), StandardCharsets.UTF_8), output);
    }

    @Override
    protected boolean showMetaData() {
        return true;
    }
}
