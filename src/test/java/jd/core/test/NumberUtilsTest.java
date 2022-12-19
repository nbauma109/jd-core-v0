package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/*
 * Test for duplicate local variable bug fix.
 */
public class NumberUtilsTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/lang3/math/NumberUtils");
        assertEquals(IOUtils.toString(getClass().getResource("NumberUtils.txt"), StandardCharsets.UTF_8), output);
    }
}
