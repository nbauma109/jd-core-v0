package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class DeclareTest extends AbstractTestCase {

    @Test
    public void test() throws Exception {
        String output = decompile("jd/core/test/Declare");
        assertEquals(IOUtils.toString(getClass().getResource("Declare.txt"), StandardCharsets.UTF_8), output);
    }
}
