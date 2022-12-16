package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class LocalVariablesTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String internalClassName = "jd/core/test/LocalVariables";
        String output = decompile(internalClassName);
        assertEquals(IOUtils.toString(getClass().getResource("LocalVariables.txt"), StandardCharsets.UTF_8), output);
    }
}
