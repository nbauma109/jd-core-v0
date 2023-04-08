package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class JavaClassTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/bcel/classfile/JavaClass");
        assertEquals(IOUtils.toString(getClass().getResource("JavaClass.txt"), StandardCharsets.UTF_8), output);
    }
}
