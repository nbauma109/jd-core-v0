package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class MacroTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("smetana/core/Macro");
        assertEquals(IOUtils.toString(getClass().getResource("Macro.txt"), StandardCharsets.UTF_8), output);
    }
}
