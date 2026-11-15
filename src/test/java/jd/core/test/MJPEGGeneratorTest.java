package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class MJPEGGeneratorTest extends AbstractTestCase {

    @Test
    public void test() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/plantuml-1.2023.12-removed-classes.jar")) {
            String output = decompile("net/sourceforge/plantuml/mjpeg/MJPEGGenerator", new CompositeLoader(in));
            assertEquals(IOUtils.toString(getClass().getResource("MJPEGGenerator.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Override
    protected boolean recompile() {
        // The classes were removed from PlantUML and their dependencies changed: only compare the decompiled source
        return false;
    }
}
