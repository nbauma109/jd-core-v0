package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/*
 * Test for CountDuploadVisitor.
 */
public class MethodUtilsTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("jd/core/test/MethodUtils");
        assertEquals(IOUtils.toString(getClass().getResource("MethodUtils.txt"), StandardCharsets.UTF_8), output);
    }
}
