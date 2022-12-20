package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/*
 * Test for CountDuploadVisitor.
 */
public class MethodBeanTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("jd/core/test/MethodBean");
        assertEquals(IOUtils.toString(getClass().getResource("MethodBean.txt"), StandardCharsets.UTF_8), output);
    }
}
