package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ReflectionUtilTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("jd/core/test/ReflectionUtil");
        assertEquals(IOUtils.toString(getClass().getResource("ReflectionUtil.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void test2() throws Exception {
        String output = decompile("jd/core/test/ReflectionUtil2");
        assertEquals(IOUtils.toString(getClass().getResource("ReflectionUtil2.txt"), StandardCharsets.UTF_8), output);
    }
}
