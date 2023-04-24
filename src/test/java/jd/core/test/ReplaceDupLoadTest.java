package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ReplaceDupLoadTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("jd/core/test/ReplaceDupLoad", "16");
        assertEquals(IOUtils.toString(getClass().getResource("ReplaceDupLoad.txt"), StandardCharsets.UTF_8), output);
    }
}
