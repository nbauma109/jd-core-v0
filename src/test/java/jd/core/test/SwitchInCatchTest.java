package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class SwitchInCatchTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("jd/core/test/SwitchInCatch");
        assertEquals(IOUtils.toString(getClass().getResource("SwitchInCatch.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void test2() throws Exception {
        String output = decompile("jd/core/test/SwitchInCatch2");
        assertEquals(IOUtils.toString(getClass().getResource("SwitchInCatch2.txt"), StandardCharsets.UTF_8), output);
    }
    
    @Override
    protected boolean recompile() {
        return false;
    }
}
