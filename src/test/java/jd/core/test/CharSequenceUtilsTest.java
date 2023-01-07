package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class CharSequenceUtilsTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/lang3/CharSequenceUtils");
        assertEquals(IOUtils.toString(getClass().getResource("CharSequenceUtils.txt"), StandardCharsets.UTF_8), output);
    }
}
