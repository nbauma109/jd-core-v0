package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class AnnotationEntryTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/bcel/classfile/AnnotationEntry");
        assertEquals(IOUtils.toString(getClass().getResource("AnnotationEntry.txt"), StandardCharsets.UTF_8), output);
    }
}
