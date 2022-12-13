package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class SwitchEnumTest extends AbstractTestCase {
    @Test
    public void testSwitchEnum() throws Exception {
        String internalClassName = "jd/core/test/SwitchEnum";
        String output = decompile(internalClassName);
        assertEquals(IOUtils.toString(getClass().getResource("SwitchEnum.txt"), StandardCharsets.UTF_8), output);
    }
}
