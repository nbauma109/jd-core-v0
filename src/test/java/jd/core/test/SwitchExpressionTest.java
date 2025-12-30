package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class SwitchExpressionTest extends AbstractTestCase {

    @Test
    public void testSwitchExpression() throws Exception {
        String output = decompile("jd/core/test/SwitchExpression", "17");
        assertEquals(IOUtils.toString(getClass().getResource("SwitchExpression.txt"), StandardCharsets.UTF_8), output);
    }
}
