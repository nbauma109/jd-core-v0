package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Assume;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ReconstructionShapesTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        // The shapes of the bytecode, hence the fixture, are those of javac
        Assume.assumeTrue("javac".equals(System.getProperty("test.compiler", "javac")));
        String output = decompile("jd/core/test/ReconstructionShapes");
        assertEquals(IOUtils.toString(getClass().getResource("ReconstructionShapes.txt"), StandardCharsets.UTF_8), output);
    }
}
