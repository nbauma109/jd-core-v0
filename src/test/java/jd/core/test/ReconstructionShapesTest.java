package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ReconstructionShapesTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("jd/core/test/ReconstructionShapes");
        assertEquals(IOUtils.toString(expectedResource("ReconstructionShapes.txt"), StandardCharsets.UTF_8), output);
    }
}
