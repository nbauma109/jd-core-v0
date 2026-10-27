package jd.core.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

/** Decompiles classes compiled by javac 21, whatever the JDK running the tests. */
public class Java21ConstructsJarTest extends AbstractTestCase {

    @Override
    protected boolean recompile() {
        return false;
    }

    @Test
    public void patternsAndTryWithResourcesAreReconstructed() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Constructs", new ZipLoader(in));
            assertFalse(output, output.contains("Byte code"));
            assertFalse(output, output.contains("addSuppressed"));
            assertFalse(output, output.contains("MatchException"));
            assertTrue(output, output.contains("instanceof String s"));
            assertTrue(output, java.util.regex.Pattern.compile("instanceof [\\w.]*Pair\\(").matcher(output).find());
            assertTrue(output, java.util.regex.Pattern.compile("case [\\w.]*Circle\\(").matcher(output).find());
            assertTrue(output, output.contains(" when "));
            assertTrue(output, output.contains("case null"));
            assertTrue(output, output.contains("try ("));
        }
    }

    @Test
    public void otherConstructsDecompileWithoutErrors() throws IOException {
        for (String name : new String[] { "s/T1", "s/L1", "s/P1", "s/W1", "s/W2", "s/C13", "s/C14", "s/C15", "s/C17",
                "s/G1", "s/K1", "s/C11", "s/C04", "s/C09", "s/C12", "s/D1", "s/D2", "More", "Extra", "Third", "Fourth", "Fifth", "Sixth", "Eighth" }) {
            try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
                String output = decompile(name, new ZipLoader(in));
                assertFalse(name + "\n" + output, output.contains("Byte code"));
                assertFalse(name + "\n" + output, output.contains("<failed"));
            }
        }
    }

    @Test
    public void patternWithoutCastAndSlotReuse() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Fourth", new ZipLoader(in));
            assertFalse(output, output.contains("Byte code"));
            assertTrue(output, java.util.regex.Pattern.compile("case Object \\w+ when ").matcher(output).find());
            assertTrue(output, java.util.regex.Pattern.compile("int x;").matcher(output).find());
        }
    }

    @Test
    public void lastCaseOfSealedStatementSwitchStaysInTheSwitch() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Fifth", new ZipLoader(in));
            String compact = output.replaceAll("\\s+", " ");
            assertTrue(output, compact.contains("x: return \"x\" + x.c();"));
            assertTrue(output, compact.contains("default: return \"other\";"));
        }
    }

    @Test
    public void enumLabelsAndTotalPatterns() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Sixth", new ZipLoader(in));
            assertTrue(output, java.util.regex.Pattern.compile("case [\\w.]*Fifth2\\.P ->").matcher(output).find());
            assertTrue(output, java.util.regex.Pattern.compile("case [\\w.]*Fifth2 x ->").matcher(output).find());
            assertTrue(output, output.contains("String s = \"fallback\""));
        }
    }

    @Test
    public void tryWithResourcesInFinally() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Eighth", new ZipLoader(in));
            assertFalse(output, output.contains("Byte code"));
        }
    }

    @Test
    public void nestedRecordPatternsFallBackToByteCode() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Nin", new ZipLoader(in));
            // Nested patterns are not reconstructed; the byte code is shown rather than wrong source
            assertTrue(output, output.contains("Byte code"));
            assertFalse(output, output.contains("localPair"));
        }
    }

    @Test
    public void castsAndSelectorsAreOnlyRemovedWhenSafe() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Ninth", new ZipLoader(in));
            assertTrue(output, output.contains("((String)c.get()).length()"));
            assertTrue(output, output.contains("unused = sideEffect();"));
            assertTrue(output, output.contains("switch (o)"));
        }
    }

    @Test
    public void codeSharedByTheCasesStaysAfterTheSwitch() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String compact = decompile("Tenth", new ZipLoader(in)).replaceAll("/\\*[^*]*\\*/", "").replaceAll("\\s+", " ");
            assertTrue(compact, compact.contains("break; default: } sink(\"after\");"));
        }
    }

    @Test
    public void tryWithResourcesInsideALabeledStatement() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Eleventh", new ZipLoader(in));
            assertFalse(output, output.contains("Byte code"));
            assertTrue(output, output.contains("try ("));
        }
    }
}
