package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class AccessFlagsTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/bcel/classfile/AccessFlags");
        assertEquals(IOUtils.toString(getClass().getResource("AccessFlags.txt"), StandardCharsets.UTF_8), output);
    }
}
