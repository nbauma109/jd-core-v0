package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class MethodGenTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/bcel/generic/MethodGen");
        assertEquals(IOUtils.toString(getClass().getResource("MethodGen.txt"), StandardCharsets.UTF_8), output);
    }
}
