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
            assertTrue(output, output.contains("TwentySixth.R r; if (b)"));
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

    @Test
    public void cleanupOnFailureOnlyIsNotATryWithResources() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("ThirtyFifth", new ZipLoader(in));
            assertFalse(output, output.contains("try (ThirtyFifth.Res"));
            assertTrue(output, output.contains("addSuppressed"));
        }
    }

    @Test
    public void wildcardBoundedByTypeVariableKeepsIntermediateCast() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("ThirtySixth", new ZipLoader(in));
            assertTrue(output, output.contains("(List<String>)(List<?>)numbers()"));
        }
    }

    @Test
    public void rawCastBeforeArrayAccessIsKept() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("ThirtySeventh", new ZipLoader(in));
            assertTrue(output, output.contains("((ThirtySeventh.Box)c.get()).array()[0]"));
        }
    }

    @Test
    public void overloadedNullCallsAndWiderLocalsAreKept() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("ThirtyEighth", new ZipLoader(in));
            assertTrue(output, output.contains("((ThirtyEighth.Box)c.get()).m("));
            assertFalse(output, output.contains("instanceof String s"));
        }
    }

    @Test
    public void copilotReportedShapes() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("ThirtyNinth", new ZipLoader(in));
            assertTrue(output, output.contains("case String s:"));
            assertTrue(output, output.contains("ThirtyNinth.Res r;"));
            assertTrue(output, output.contains("((ThirtyNinth.Box)c.get()).names()"));
            assertTrue(output, output.contains("((ThirtyNinth.Box)c.get()).value = Integer.valueOf(7)"));
            assertTrue(output, output.contains("super.touch()"));
        }
    }

    @Test
    public void rawCastBeforeMemberThrowingTypeVariableIsKept() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Fortieth", new ZipLoader(in));
            assertTrue(output, output.contains("((Fortieth.Box)c.get()).run()"));
        }
    }

    @Test
    public void rawCastBeforeThrownResultIsKept() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("FortyFirst", new ZipLoader(in));
            assertTrue(output, output.contains("((FortyFirst.Box)c.get()).exception()"));
        }
    }

    @Test
    public void uncheckedCastIsNotAParameterizedPattern() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("FortySecond", new ZipLoader(in));
            assertFalse(output, output.contains("instanceof List<String>"));
            assertTrue(output, output.contains("List<String> xs = (List)o;"));
        }
    }

    @Test
    public void parameterizedPatternsNeedCompatibleArguments() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("FortyThird", new ZipLoader(in));
            assertTrue(output, output.contains("ArrayList<String> xs = (ArrayList)o;"));
            assertTrue(output, output.contains("instanceof ArrayList<String>"));
        }
    }

    @Test
    public void rearrangedArgumentsAndPolymorphicCallsKeepTheirForm() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("FortyFourth", new ZipLoader(in));
            assertFalse(output, output.contains("instanceof FortyFourth.Flip<"));
            assertTrue(output, output.contains("Integer.valueOf(i)"));
        }
    }

    @Test
    public void componentPatternsKeepTheTypeOfTheComponent() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("FortyFifth", new ZipLoader(in));
            assertFalse(output, output.contains("R(java.util.List<Integer>"));
            assertTrue(output, output.contains("FortyFifth.R(java.util.List<String> xs)") || output.contains("FortyFifth.R(List<String> xs)"));
        }
    }

    @Test
    public void nullInitializerIsNotTypedWithVar() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("FortySixth", new ZipLoader(in));
            assertFalse(output, output.contains("var value"));
            assertFalse(output, output.contains("static record R"));
        }
    }

    @Test
    public void chainedParameterizedResultsReassignedLocalsAndUserDefaults() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("FortySeventh", new ZipLoader(in));
            assertTrue(output, output.contains("((FortySeventh.Box)c.get()).fixed().get(0)"));
            assertFalse(output, output.contains("var v"));
            assertTrue(output, output.contains("throw new MatchException(null, null)"));
        }
    }

    @Test
    public void negatedTestIsNotARecordPattern() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("FortyEighth", new ZipLoader(in));
            assertFalse(output, output.contains("instanceof FortyEighth.R(int"));
            assertTrue(output, output.contains("MatchException"));
        }
    }

    @Test
    public void fieldWriteOfTypeVariableListKeepsRawCast() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("FortyNinth", new ZipLoader(in));
            assertTrue(output, output.contains("((FortyNinth.Box)c.get()).field = value"));
        }
    }

    @Test
    public void extractionAfterTheBranchIsNotFolded() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Fiftieth", new ZipLoader(in));
            assertFalse(output, output.contains("instanceof Fiftieth.R(int"));
        }
    }

    @Test
    public void fieldWriteOfForeignCallResultKeepsRawCast() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("FiftyFirst", new ZipLoader(in));
            assertTrue(output, output.contains("((FiftyFirst.Box)c.get()).field = other.values()"));
        }
    }

    @Test
    public void recordAssignedToAnExistingLocalIsNotFolded() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("FiftySecond", new ZipLoader(in));
            assertFalse(output, output.contains("instanceof FiftySecond.R(int"));
        }
    }

    @Test
    public void bridgesHandlesTotalPatternsAndOlderUnconditionalTests() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("FiftyThird", new ZipLoader(in));
            assertTrue(output, output.contains("(java.util.List<S>)(java.util.List<?>)numbers()"));
            assertTrue(output, output.contains("h.invokeExact(((FiftyThird.Box)c.get()).get())"));
            assertTrue(output, output.contains("case CharSequence c:"));
            assertFalse(output, output.contains("default:"));
            String older = decompile("FiftyFourth", new ZipLoader(getClass().getResourceAsStream("/constructs-jdk21.jar")));
            assertFalse(older, older.contains("instanceof CharSequence s"));
        }
    }

    @Test
    public void typeVariableNamesAndConstructorParametersStayOutOfMembers() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("FiftyFifth", new ZipLoader(in));
            assertFalse(output, output.contains("record T"));
            assertTrue(output, output.contains("T generic(T value)"));
            assertFalse(output, output.contains("println(n)"));
        }
    }

    @Test
    public void sameNamedTypeVariablesKeepTheBridge() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("FiftySixth", new ZipLoader(in));
            assertTrue(output, output.contains("(List<T>)(List<?>)c.numbers()"));
        }
    }

    @Test
    public void calledMethodTypeParametersAndSeveralConstructors() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("FiftySeventh", new ZipLoader(in));
            assertTrue(output, output.contains("(List<T>)(List<?>)strings()"));
            assertFalse(output, output.contains("\"zero\"") && output.contains("\"one\""));
        }
    }

    @Test
    public void nestedAndGuardedRecordPatterns() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("FiftyNinth", new ZipLoader(in));
            assertFalse(output, output.contains("Byte code"));
            assertFalse(output, output.contains("MatchException"));
            String compact = output.replaceAll("\\s+", " ");
            assertTrue(output, compact.contains("x instanceof FiftyNinth.Pair(FiftyNinth.Box(String s), FiftyNinth.Box(Integer i))"));
            assertTrue(output, compact.contains("case FiftyNinth.Box(String s) when s.length() > 2 -> s;"));
            assertTrue(output, compact.contains("case FiftyNinth.Box(Object o) -> \"box\";"));
        }
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Sixty3rd", new ZipLoader(in));
            assertFalse(output, output.contains("Byte code"));
            assertFalse(output, output.contains("MatchException"));
            String compact = output.replaceAll("\\s+", " ");
            assertTrue(output, compact.contains("case Sixty3rd.Pair(Sixty3rd.Box(String s), Sixty3rd.Box b) -> s + b;"));
            assertTrue(output, compact.contains("case Sixty3rd.Box(CharSequence c) when c.length() > 3 ->"));
            assertTrue(output, compact.contains("case Sixty3rd.Wrap(Sixty3rd.Num(int n)) ->"));
            assertTrue(output, compact.contains("x instanceof Sixty3rd.Box(Sixty3rd.Num(int n))) && n > 2"));
        }
    }

    @Test
    public void recordComponentsFollowedByLoops() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("FiftyEighth", new ZipLoader(in));
            assertFalse(output, output.contains("Byte code"));
            String compact = output.replaceAll("\\s+", " ");
            assertTrue(output, compact.contains("case FiftyEighth.A(List<String> l): { for (String x : l) {"));
            assertTrue(output, compact.contains("for (String x : a.l()) {"));
            assertTrue(output, compact.contains("for (int i = 0; i < b.n(); i++) {"));
        }
    }

    @Test
    public void localTypesReferringToEachOtherOrSharingANameAreMembers() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Sixtieth", new ZipLoader(in));
            String compact = output.replaceAll("\\s+", " ");
            assertTrue(output, compact.contains("return new Sixtieth.Outer(new Sixtieth.Inner(1));"));
            assertTrue(output, compact.contains("static record Outer(Sixtieth.Inner i)"));
            assertTrue(output, compact.contains("return new Sixtieth.Member_1(1);"));
            assertTrue(output, compact.contains("static record Member_1(int a)"));
            assertTrue(output, compact.contains("class Member {}"));
        }
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Sixty2nd", new ZipLoader(in));
            String compact = output.replaceAll("\\s+", " ");
            assertTrue(output, compact.contains("return new Sixty2nd.R(1);"));
            assertTrue(output, compact.contains("return new Sixty2nd.R_2(\"x\");"));
            assertTrue(output, compact.contains("static record R_2(String b)"));
        }
    }

    @Test
    public void conditionalOfNullsIsNotInferred() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Sixty4th", new ZipLoader(in));
            assertFalse(output, output.contains("var value"));
        }
    }

    @Test
    public void monitorsKeepRawCastsAndGeneratedNamesDoNotCollide() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Sixty5th", new ZipLoader(in));
            assertTrue(output, output.replaceAll("\\s+", "").contains("synchronized(((Sixty5th.Box)c.get()).get())"));
        }
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Sixty6th", new ZipLoader(in));
            assertFalse(output, output.contains("record R_1(int c)"));
        }
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Sixty7th", new ZipLoader(in));
            assertTrue(output, output.replaceAll("\\s+", "").contains("if(r==null)return;"));
        }
    }

    @Test
    public void textBlocksEscapeWhitespaceAndQuotes() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Cov1", new ZipLoader(in));
            assertFalse(output, output.contains("<failed"));
            assertTrue(output, output.contains("\\ttab"));
            assertTrue(output, output.contains("\\rcr"));
            assertTrue(output, output.contains("back\\bspace"));
            assertTrue(output, output.contains("form\\ffeed"));
            assertTrue(output, output.contains("\\001ctl"));
            assertTrue(output, output.contains("trailing\\s"));
            assertTrue(output, output.contains("back\\\\slash"));
            assertTrue(output, output.contains("case \"a\" -> 1;"));
            assertTrue(output, output.contains("case 2 -> 2;"));
        }
    }

    @Test
    public void wildcardCastsInReturnsAndStores() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Cov2", new ZipLoader(in));
            assertFalse(output, output.contains("Byte code"));
            assertTrue(output, output.contains("return (List<Integer>)numbers();"));
            assertTrue(output, output.contains("return (List<T>)(List<?>)shadow();"));
        }
    }

    @Test
    public void resourcesOfSeveralKindsAndConsecutiveStatements() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Cov3", new ZipLoader(in));
            assertFalse(output, output.contains("Byte code"));
            String compact = output.replaceAll("\\s+", " ");
            assertTrue(output, compact.contains("try (Cov3.R a = open(); Cov3.R b = open();) { return a.read() + b.read(); }"));
            assertTrue(output, compact.contains("try (Cov3.R a = open()) { a.read(); } try (Cov3.R a = open()) { return a.read(); }"));
            assertFalse(output, compact.contains("Cov3.R a; try"));
        }
    }

    @Test
    public void patternsOfOtherClassesAndSwitchShapes() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Cov4", new ZipLoader(in));
            assertFalse(output, output.contains("Byte code"));
            String compact = output.replaceAll("\\s+", " ");
            assertTrue(output, compact.contains("x instanceof Rec(String s, int n)) && n > 1"));
            assertTrue(output, compact.contains("case Rec(Integer i, int n) -> \"i\" + i + n;"));
            assertTrue(output, compact.contains("case Cov4.Color x when x == Cov4.Color.RED -> 1;"));
            assertTrue(output, compact.contains("case Cov4.B(int n) when n > 3 -> 2;"));
            assertTrue(output, compact.contains("case null -> \"null\";"));
        }
    }

    @Test
    public void rawCastsAreKeptWhereTheyAreRuntimeChecks() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Cov5", new ZipLoader(in));
            assertFalse(output, output.contains("Byte code"));
            assertTrue(output, output.contains("a[0] = ((Cov5.Box)c.get()).get();"));
            assertTrue(output, output.contains("return (Object[])((Cov5.Box)c.get()).get();"));
            assertTrue(output, output.contains("return a[b.get().intValue()];"));
            assertTrue(output, output.contains("return m.get(\"k\").get();"));
        }
    }

    @Test
    public void writtenConstructorParametersAreNotInitializers() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar")) {
            String output = decompile("Cov6", new ZipLoader(in));
            assertFalse(output, output.contains("n = effect()"));
        }
    }
}
