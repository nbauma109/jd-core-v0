package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class OuterTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String internalClassName = "jd/core/test/Outer";
        String output = decompile(internalClassName);
        assertEquals(IOUtils.toString(getClass().getResource("Outer.txt"), StandardCharsets.UTF_8), output);
    }
}
