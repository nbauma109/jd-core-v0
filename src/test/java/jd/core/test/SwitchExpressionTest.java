package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class SwitchExpressionTest extends AbstractTestCase {

    @Test
    public void testSwitchExpression() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/switch-expression-enum-jdk21.0.8.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/SwitchExpression", loader, "17");
            assertEquals(IOUtils.toString(getClass().getResource("SwitchExpression.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Test
    public void testSwitchExpressionECJ() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/switch-expression-enum-ecj21.0.8.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/SwitchExpression", loader, "17");
            assertEquals(IOUtils.toString(getClass().getResource("SwitchExpression.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
