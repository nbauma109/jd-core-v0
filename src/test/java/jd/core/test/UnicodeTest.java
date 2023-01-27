package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class UnicodeTest extends AbstractTestCase {
    @Test
    public void testUnicode() throws Exception {
        String internalClassName = "jd/core/test/你好";
        String output = decompile(internalClassName);
        assertEquals(IOUtils.toString(getClass().getResource("Unicode.txt"), StandardCharsets.UTF_8), output);
    }
}
