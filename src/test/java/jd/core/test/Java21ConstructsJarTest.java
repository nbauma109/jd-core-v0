package jd.core.test;

import static org.junit.Assert.assertEquals;
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

    @Override
    protected boolean showLineNumbers() {
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

    @Test
    public void textBlocksAndBoxingOfArguments() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Vf", new ZipLoader(in));
            assertTrue(output, output.contains("String text = \"\"\""));
            assertTrue(output, output.contains("I can use \"quotes\" in it."));
            assertTrue(output, output.contains("map.get(0).orElse(null)"));
            assertTrue(output, output.contains("x <<= 3;"));
        }
    }

    @Test
    public void boxingWideningAndTextBlockIndentation() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Twelfth", new ZipLoader(in));
            assertTrue(output, output.contains("accept(Long.valueOf(i))"));
            assertTrue(output, output.contains("\"  a\\n  b\\n  c\\n  d\""));
            assertTrue(output, output.contains("String indentedNewline = \"\"\""));
        }
    }

    @Test
    public void whitespaceCharactersSurviveTextBlocks() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Thirteenth", new ZipLoader(in));
            assertTrue(output, output.contains("a\\f\n"));
            assertTrue(output, output.contains("a\\001\n") && output.contains("b\\034\n"));
            // Whitespace the compiler would strip cannot be escaped: the string stays a literal
            assertTrue(output, output.contains("String unicodeSpace = \"a\u2003\\nb"));
        }
    }

    @Test
    public void userHandlersAndWildcardCastsAreKept() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Fourteenth", new ZipLoader(in));
            assertTrue(output, output.contains("throw new MatchException(\"user\", e);"));
            assertTrue(output, output.contains("return make();"));
        }
    }

    @Test
    public void wildcardCastsAndQualifiedEnumLabelsOfOtherPackages() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("q/Switcher", new ZipLoader(in));
            assertTrue(output, output.contains("(List<String>)(List<?>)numbers()"));
            assertTrue(output, output.contains("(List<Number>)supers()"));
            assertTrue(output, output.contains("case p.Outer.E.A ->") || output.contains("import p.Outer.E;"));
        }
    }

    @Test
    public void explicitCloseAndArraysOfParameterizedTypes() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Fifteenth", new ZipLoader(in));
            assertTrue(output, output.replaceAll("\\s+", " ").contains("try (Fifteenth.R r = new Fifteenth.R()) { r.close(); }"));
            assertTrue(output, output.contains("(List<String>[])(List<?>[])numbers()"));
        }
    }

    @Test
    public void recordPatternsNeedEveryComponentAndExplicitCloseBeforeThrowIsKept() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Sixteenth", new ZipLoader(in));
            String compact = output.replaceAll("\\s+", " ");
            assertTrue(output, compact.contains("instanceof Sixteenth.Pair(int x, int y)"));
            assertFalse(output, compact.contains("Pair(int b)"));
            assertTrue(output, compact.contains("r.close(); throw new IllegalStateException();"));
        }
    }

    @Test
    public void closeInsideALoopAndRecordVariableStillNeeded() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String compact = decompile("Seventeenth", new ZipLoader(in)).replaceAll("\\s+", " ");
            assertTrue(compact, compact.contains("while (again()) r.close();"));
            assertTrue(compact, compact.contains("continue;"));
            assertTrue(compact, compact.contains("return r.x() + b;"));
            assertFalse(compact, compact.contains("Box(int b)"));
        }
    }

    @Test
    public void usedComponentTemporariesAndCastsOfInstanceofOperands() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String compact = decompile("Eighteenth", new ZipLoader(in)).replaceAll("\\s+", " ");
            assertTrue(compact, compact.contains("return a + b;"));
            assertFalse(compact, compact.contains("Box(int b)"));
            assertTrue(compact, compact.contains("instanceof Eighteenth.Box(int x)"));
            assertTrue(compact, compact.contains("(String)xs.get(0) instanceof"));
            assertTrue(compact, compact.contains("xs.get(0).length()"));
        }
    }

    @Test
    public void unwrappedAccessorsUserDefaultsAndNarrowBoxing() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String compact = decompile("Nineteenth", new ZipLoader(in)).replaceAll("\\s+", " ");
            assertTrue(compact, compact.contains("accept(Integer.valueOf(b)); accept(i);"));
            assertTrue(compact, compact.contains("default -> throw new MatchException(null, null);"));
            assertFalse(compact, compact.contains("R(int b)"));
            assertTrue(compact, compact.contains("case Nineteenth.A a -> 1; case Nineteenth.B b -> 2; }"));
        }
    }

    @Test
    public void existingLocalsAreNotRedeclaredAndBlankTextStaysALiteral() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String compact = decompile("Twentieth", new ZipLoader(in)).replaceAll("\\s+", " ");
            assertTrue(compact, compact.contains("String s = \"before\"; if ((o instanceof String)) { s = (String)o; }"));
            assertTrue(compact, compact.contains("if ((o instanceof String s)) { return s; }"));
            assertTrue(compact, compact.contains("String blanks = \" \\n \\n \\n \";"));
            assertTrue(compact, compact.contains("String text = \"\"\" a b c d \"\"\";"));
        }
    }

    @Test
    public void explicitNullCheckBeforeASwitchAcceptingNullIsKept() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String compact = decompile("TwentyFirst", new ZipLoader(in)).replaceAll("\\s+", " ");
            assertTrue(compact, compact.contains("Objects.requireNonNull(o); return switch (o) { case null ->"));
            assertFalse(compact, compact.contains("generated(Object o) { Objects"));
        }
    }

    @Test
    public void boundDirectionAndRawReceiverCasts() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("TwentySecond", new ZipLoader(in));
            assertTrue(output, output.contains("(List<Integer>)(List<?>)lower()"));
            assertTrue(output, output.contains("((List)c.get()).add(\"x\")"));
            assertTrue(output, output.contains("m.get(0).orElse(null)"));
        }
    }

    @Test
    public void restartSlotReuseAndSharedVariable() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("TwentyThird", new ZipLoader(in));
            assertTrue(output, output.contains("int n = 1;"));
            assertTrue(output, output.contains("sink(n)"));
            assertFalse(output, output.contains("instanceof String s"));
            assertTrue(output, output.contains("return s;"));
        }
    }

    @Test
    public void primitiveSlotOverloadAndShadowedLocalType() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("TwentyFourth", new ZipLoader(in));
            assertTrue(output, output.contains("int x = 7;"));
            assertTrue(output, output.contains("((TwentyFourth.Box)c.get()).get()"));
            String shadowed = decompile("p/Shadow", new ZipLoader(getClass().getResourceAsStream("/constructs-jdk21.jar")));
            assertTrue(shadowed, shadowed.contains("enum E"));
            assertTrue(shadowed, shadowed.contains("p.Shadow.E.A.name()"));
        }
    }

    @Test
    public void castContextsLaterRedeclarationAndShadowedJavaLang() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("TwentyFifth", new ZipLoader(in));
            assertTrue(output, output.contains("((TwentyFifth.Box)c.get()).get() instanceof Integer"));
            assertTrue(output, output.contains("try (TwentyFifth.R r = new TwentyFifth.R())"));
            assertTrue(output, output.contains("    TwentyFifth.R r = new TwentyFifth.R();"));
            assertTrue(output, output.contains("java.lang.String hello()"));
        }
    }

    @Test
    public void siblingRedeclarationsChainedCastsAndGenericBindings() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("TwentySixth", new ZipLoader(in));
            assertEquals(output, 2, output.split("TwentySixth.R r = new TwentySixth.R\\(\\);", -1).length - 1);
            assertTrue(output, output.contains("((TwentySixth.Box)c.get()).get().toString()"));
            assertTrue(output, output.contains("instanceof ArrayList<String> a"));
        }
    }

    @Test
    public void boundedVariableCastsAndGenericSwitchLabels() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("TwentySeventh", new ZipLoader(in));
            assertTrue(output, output.contains("(List<T>)(List<?>)numbers()"));
            String labels = decompile("TwentyEighth", new ZipLoader(getClass().getResourceAsStream("/constructs-jdk21.jar")));
            assertTrue(labels, labels.contains("case java.util.ArrayList<String> a:"));
            assertTrue(labels, labels.contains("case java.util.LinkedList<String> l:"));
        }
    }

    @Test
    public void unconditionalCastAfterEmptyInstanceofIsNotABinding() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("TwentyNinth", new ZipLoader(in));
            assertFalse(output, output.contains("instanceof String s"));
            assertTrue(output, output.contains("(String)o"));
        }
    }

    @Test
    public void rawCastBeforeGenericMethodArgumentIsKept() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Thirtieth", new ZipLoader(in));
            assertTrue(output, output.contains("((Thirtieth.Box)c.get()).values()"));
        }
    }

    @Test
    public void rawCastBeforeParameterizedResultIsKept() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("ThirtyFirst", new ZipLoader(in));
            assertTrue(output, output.contains("((ThirtyFirst.Box)c.get()).values()"));
        }
    }

    @Test
    public void userWrittenMatchExceptionHandlerIsKept() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("ThirtySecond", new ZipLoader(in));
            assertFalse(output, output.contains("instanceof ThirtySecond.R(int"));
            assertTrue(output, output.contains("\"custom\""));
        }
    }

    @Test
    public void promotedLocalTypeDoesNotHideSamePackageClass() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("p/ThirtyThird", new ZipLoader(in));
            assertTrue(output, output.contains("p.X g()"));
            assertTrue(output, output.contains("new p.X()"));
        }
    }

    @Test
    public void handlersWithWiderRangesAreKept() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("ThirtyFourth", new ZipLoader(in));
            assertFalse(output, output.contains("instanceof ThirtyFourth.R(int"));
            assertFalse(output, output.contains("try (ThirtyFourth.Res"));
            assertTrue(output, output.contains("addSuppressed"));
        }
    }
}
