package jd.core.test;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VineflowerSamplesTest extends AbstractTestCase {

    @Test
    public void floatConstantWidenedToDoubleKeepsFloatLiteral() throws IOException {
        String output = decompile("jd/core/test/VineflowerSamples", "17");
        assertTrue(output, output.contains("0.2F"));
        assertFalse(output, output.contains("0.20000000298023224"));
    }

    @Test
    public void namelessLocalClassIsDeclaredWithVar() throws IOException {
        String output = decompile("jd/core/test/VineflowerSamples", "17");
        assertTrue(output, output.contains("var "));
        assertTrue(output, output.contains(".println(\"goodbye, world!\")"));
    }

    @Test
    public void localClassOnlyOverridingKeepsItsSuperType() throws IOException {
        String output = decompile("jd/core/test/VineflowerSamples", "17");
        assertTrue(output, output.contains("Runnable "));
        assertFalse(output, output.contains("var local"));
    }

    @Test
    public void instanceInitializersOfEnumConstantsAreKept() throws IOException {
        String output = decompile("jd/core/test/VineflowerSamples$EnumWithInitializers", "17");
        assertTrue(output, output.contains("System.out.println(\"A\")"));
        assertTrue(output, output.contains("System.out.println(\"B\")"));
    }

    @Test
    public void instanceInitializerOfAnonymousClassIsKept() throws IOException {
        String output = decompile("jd/core/test/VineflowerSamples", "17");
        assertTrue(output, output.contains("System.out.println(\"init\")"));
    }
}
