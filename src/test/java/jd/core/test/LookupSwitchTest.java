package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class LookupSwitchTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String internalClassName = "jd/core/test/LookupSwitch";
        String output = decompile(internalClassName);
        assertEquals(IOUtils.toString(getClass().getResource("LookupSwitch.txt"), StandardCharsets.UTF_8), output);
    }
}
