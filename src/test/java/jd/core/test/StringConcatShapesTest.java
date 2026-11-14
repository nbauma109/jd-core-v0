package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Assume;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class StringConcatShapesTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        // The jar was compiled with javac: the bytecode, hence the fixture, depends on the compiler
        Assume.assumeTrue("javac".equals(System.getProperty("test.compiler", "javac")));
        try (InputStream in = getClass().getResourceAsStream("/string-concat-java8.jar")) {
            String output = decompile("jd/core/test/StringConcatShapes", new CompositeLoader(in));
            assertEquals(IOUtils.toString(getClass().getResource("StringConcatShapes.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
