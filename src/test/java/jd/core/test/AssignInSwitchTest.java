package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class AssignInSwitchTest extends AbstractTestCase {

    @Test
    public void test() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/assign-in-switch-jdk1.8.0_331.jar")) {
            Loader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/AssignInSwitch", loader);
            assertEquals(IOUtils.toString(getClass().getResource("AssignInSwitch.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
