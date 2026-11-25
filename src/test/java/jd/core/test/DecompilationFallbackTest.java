package jd.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

/** Constructs found while auditing JARs, whose methods used to be written as byte code or failed their class. */
public class DecompilationFallbackTest extends AbstractTestCase {

    private static final String PACKAGE = "jd/core/test/fallbacks/";
    private static final String JAVAC_JAR = "/decompilation-fallbacks-javac17.jar";

    @Test
    public void testSwitchExpressionWithThrowingDefault() throws Exception {
        assertDecompiles("ThrowingDefaultSwitch");
    }

    @Test
    public void testStringLiteralsAndArrayConstructorReferences() throws Exception {
        assertDecompiles("LiteralsAndArrayReferences");
    }

    @Test
    public void testReturnedTypeWithMoreTypeArguments() throws Exception {
        assertDecompiles("WildcardGatherer");
    }

    @Test
    public void testStringSwitchReusingLoopVariableSlots() throws Exception {
        assertDecompiles("ReusedSwitchSlots");
    }

    @Test
    public void testFinallyCopiedBeforeReturn() throws Exception {
        assertDecompiles("ReturnInFinally");
    }

    @Test
    public void testLambdaArgumentsCapturedWithoutLocalVariableEntry() throws Exception {
        assertDecompiles("CapturedLambdaArguments", "/decompilation-fallbacks-ecj17.jar");
    }

    @Test
    public void testFailedConstructorDoesNotFailItsClass() throws Exception {
        String output = decompileFixture("FailedConstructor", JAVAC_JAR);
        assertTrue(output, output.contains("// Byte code:"));
        assertTrue(output, output.contains("System.out.println();"));
        assertTrue(output, output.contains("int size = 3;"));
    }

    private void assertDecompiles(String simpleName) throws Exception {
        assertDecompiles(simpleName, JAVAC_JAR);
    }

    private void assertDecompiles(String simpleName, String jar) throws Exception {
        String output = decompileFixture(simpleName, jar);
        assertFalse(output, output.contains("// Byte code:"));
        assertRealignedLineNumbers(PACKAGE + simpleName, output);
        assertEquals(IOUtils.toString(getClass().getResource("Fallback" + simpleName + ".txt"), StandardCharsets.UTF_8), output);
    }

    private String decompileFixture(String simpleName, String jar) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(jar)) {
            return decompile(PACKAGE + simpleName, new ZipLoader(in), "17");
        }
    }
}
