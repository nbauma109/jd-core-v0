package jd.core.test;

import org.junit.Assume;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** The output must recompile: it is checked by {@link AbstractTestCase}. The bytecode shapes are those of javac. */
public class JavacConstructsTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        Assume.assumeTrue("javac".equals(System.getProperty("test.compiler", "javac")));
        String output = decompile("jd/core/test/JavacConstructs");
        assertTrue(output, output.contains("assert"));
        assertTrue(output, output.contains("try ("));
        assertFalse(output, output.contains("$assertionsDisabled"));
        assertFalse(output, output.contains("localThrowable"));
        assertTrue(output, output.contains("requireNonNull"));
    }
}
