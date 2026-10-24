package jd.core.test;

import java.io.IOException;
import java.net.URI;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.IntSupplier;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.MethodGen;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Test;

import jd.core.preferences.Preferences;
import jd.core.printer.PrinterImpl;
import jd.core.process.DecompilerImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SyntheticReconstructionTest {
    @Test
    public void capturedAnonymousConstructorUsesDescriptorForOuterParameter() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    private int value = 10;
                    public java.util.function.IntSupplier reader(final int captured) {
                        return new java.util.function.IntSupplier() {
                            public int getAsInt() { return value + captured; }
                        };
                    }
                }
                """, 17);
    }

    @Test
    public void nestedAnonymousConstructorsRetainBothEnclosingInstances() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    private int value = 10;
                    public java.util.function.IntSupplier reader(final int captured) {
                        return new java.util.function.IntSupplier() {
                            public int getAsInt() {
                                return new java.util.function.IntSupplier() {
                                    public int getAsInt() { return value + captured; }
                                }.getAsInt();
                            }
                        };
                    }
                }
                """, 17);
    }

    @Test
    public void capturedVariableResolvesAcrossMultipleAnonymousClasses() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    public static java.util.function.IntSupplier reader(final int captured) {
                        return new java.util.function.IntSupplier() {
                            public int getAsInt() {
                                if (captured < 0) { return -1; }
                                return new java.util.function.IntSupplier() {
                                    public int getAsInt() {
                                        return new java.util.function.IntSupplier() {
                                            public int getAsInt() { return captured + 10; }
                                        }.getAsInt();
                                    }
                                }.getAsInt();
                            }
                        };
                    }
                }
                """, 17);
    }

    @Test
    public void staticCapturePreservesUserDollarIdentifiers() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    private static int value$ = 10;
                    public static java.util.function.IntSupplier reader(final int captured) {
                        return new java.util.function.IntSupplier() {
                            public int getAsInt() { return value$ + captured; }
                        };
                    }
                }
                """, 17);
    }

    @Test
    public void nestedEnumSwitchExpressionUsesEnclosingSwitchMap() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    private enum Kind { A, B, C }
                    private static class Middle {
                      static java.util.function.IntSupplier make(final int captured) {
                        return new java.util.function.IntSupplier() {
                            public int getAsInt() {
                                return switch (Kind.values()[captured % 3]) {
                                    case A -> 10;
                                    case B -> 17;
                                    case C -> 30;
                                };
                            }
                        };
                      }
                    }
                    public java.util.function.IntSupplier reader(int captured) { return Middle.make(captured); }
                }
                """, 17, "17");
    }

    @Test
    public void nestedEnumSwitchStatementUsesEnclosingSwitchMap() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    private enum Kind { A, B, C }
                    private static class Middle {
                      private static class Reader implements java.util.function.IntSupplier {
                        public int getAsInt() {
                            switch (Kind.B) {
                                case A: return 10;
                                case B: return 17;
                                default: return 30;
                            }
                        }
                    }
                    }
                    public java.util.function.IntSupplier reader(int captured) { return new Middle.Reader(); }
                }
                """, 17, "8");
    }

    @Test
    public void enumConstantBodyUsesTopLevelSwitchMap() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    private enum Kind { A, B, C }
                    private enum Mode {
                        FIRST {
                            int apply(Kind kind) {
                                return switch (kind) {
                                    case A -> 10;
                                    case B -> 17;
                                    case C -> 30;
                                };
                            }
                        };
                        abstract int apply(Kind kind);
                    }
                    public java.util.function.IntSupplier reader(int captured) {
                        return new java.util.function.IntSupplier() {
                            public int getAsInt() { return Mode.FIRST.apply(Kind.B); }
                        };
                    }
                }
                """, 17, "17");
    }

    @Test
    public void primitiveStaticAssignmentAccessorReturnsAssignedValue() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    private static int value;
                    public static java.util.function.IntSupplier reader(final int captured) {
                        return new java.util.function.IntSupplier() {
                            public int getAsInt() { return value = captured + 10; }
                        };
                    }
                }
                """, 17);
    }

    @Test
    public void staticReferenceCaptureIsNotAnEnclosingInstance() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    private int value = 10;
                    public static java.util.function.IntSupplier reader(int captured) {
                        final SyntheticOuter other = new SyntheticOuter();
                        return new java.util.function.IntSupplier() {
                            public int getAsInt() { return other.value + 7; }
                        };
                    }
                }
                """, 17);
    }

    @Test
    public void wideStaticAssignmentAccessorReturnsAssignedValue() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    private static long value;
                    public static java.util.function.IntSupplier reader(final int captured) {
                        return new java.util.function.IntSupplier() {
                            public int getAsInt() { return (int)(value = captured + 10L); }
                        };
                    }
                }
                """, 17);
    }

    @Test
    public void legacyCaptureStoredAfterSuperConstructorRemainsALocalVariable() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    public static java.util.function.IntSupplier reader(final int captured) {
                        return new java.util.function.IntSupplier() {
                            public int getAsInt() { return captured + 10; }
                        };
                    }
                }
                """, 17, "8", true);
    }

    @Test
    public void constructorLambdaCapturesTheLexicalEnclosingInstance() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    private int value = 10;
                    private class Metrics {
                        final java.util.function.IntSupplier supplier;
                        Metrics() { supplier = () -> value + 7; }
                    }
                    public java.util.function.IntSupplier reader(int captured) { return new Metrics().supplier; }
                }
                """, 17);
    }

    @Test
    public void emptyEnumSwitchPreservesNullCheck() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    private enum Kind { A, B }
                    public java.util.function.IntSupplier reader(int captured) {
                        return new java.util.function.IntSupplier() {
                            public int getAsInt() {
                                Kind kind = null;
                                try { switch (kind) { } }
                                catch (NullPointerException expected) { return 17; }
                                return 99;
                            }
                        };
                    }
                }
                """, 17);
    }

    @Test
    public void assertionWithBooleanEqualityIsReconstructed() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    public java.util.function.IntSupplier reader(int captured) {
                        Object object = new Object();
                        assert (object == null) == (captured < 0);
                        return new java.util.function.IntSupplier() {
                            public int getAsInt() { return 17; }
                        };
                    }
                }
                """, 17);
    }

    @Test
    public void innerSubclassWithoutStoredOuterFieldUsesLexicalInstance() throws Exception {
        Path original = compile("""
                public class SyntheticOuter {
                    private int value = 10;
                    private class Base implements java.util.function.IntSupplier {
                        private int number;
                        Base(int number) { this.number = number; }
                        public int getAsInt() { return value + number; }
                    }
                    private class Derived extends Base {
                        Derived(int number) { super(number); }
                    }
                    public java.util.function.IntSupplier reader(int captured) { return new Derived(captured); }
                }
                """, "8");
        removeUnusedOuterField(original.resolve("SyntheticOuter$Derived.class"));
        assertRoundTrip(original, 17, "8");
    }

    @Test
    public void reconstructedAssertionPreservesEnabledAndDisabledBehavior() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    public java.util.function.IntSupplier reader(int captured) {
                        Object object = new Object();
                        int result = 99;
                        try { assert (object == null) == (captured > 0) : "failure"; }
                        catch (AssertionError expected) { result = 17; }
                        final int answer = result;
                        return new java.util.function.IntSupplier() {
                            public int getAsInt() { return answer; }
                        };
                    }
                }
                """, 17);
    }

    @Test
    public void assertionAtEndOfCatchPreservesDisabledBehavior() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    public java.util.function.IntSupplier reader(int captured) {
                        return new java.util.function.IntSupplier() {
                            public int getAsInt() {
                                try {
                                    try { throw new IllegalArgumentException(); }
                                    catch (IllegalArgumentException ignored) { assert false; }
                                } catch (AssertionError expected) { return 17; }
                                return 99;
                            }
                        };
                    }
                }
                """, 17);
    }

    @Test
    public void anonymousAssertionMayUseItsEnclosingClassFlag() throws Exception {
        Path original = compile("""
                public class SyntheticOuter {
                    public java.util.function.IntSupplier reader(final int captured) {
                        assert captured > 0;
                        return new java.util.function.IntSupplier() {
                            public int getAsInt() { assert captured > 0; return 17; }
                        };
                    }
                }
                """, "8");
        Path file = original.resolve("SyntheticOuter$1.class");
        ClassGen generated = new ClassGen(new ClassParser(file.toString()).parse());
        var constants = generated.getConstantPool();
        for (var method : generated.getMethods()) {
            if (method.getCode() == null) {
                continue;
            }
            MethodGen body = new MethodGen(method, generated.getClassName(), constants);
            for (var handle : body.getInstructionList()) {
                if (handle.getInstruction() instanceof org.apache.bcel.generic.GETSTATIC field
                        && "$assertionsDisabled".equals(field.getFieldName(constants))) {
                    field.setIndex(constants.addFieldref("SyntheticOuter", "$assertionsDisabled", "Z"));
                }
            }
            generated.replaceMethod(method, body.getMethod());
        }
        generated.getJavaClass().dump(file.toString());
        assertRoundTrip(original, 17, "8");
    }

    @Test
    public void superConstructorLambdaCapturesEnclosingInstanceDirectly() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    private int value = 10;
                    private static class Base {
                        final java.util.function.IntSupplier supplier;
                        Base(java.util.function.IntSupplier supplier) { this.supplier = supplier; }
                    }
                    private class Metrics extends Base {
                        Metrics() { super(() -> SyntheticOuter.this.value + 7); }
                    }
                    public java.util.function.IntSupplier reader(int captured) { return new Metrics().supplier; }
                }
                """, 17);
    }

    @Test
    public void staticConstructorLambdaKeepsItsExplicitObjectCapture() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    private int value = 10;
                    private static class Box {
                        final java.util.function.IntSupplier supplier;
                        Box(SyntheticOuter other) { supplier = () -> other.value + 7; }
                    }
                    public java.util.function.IntSupplier reader(int captured) { return new Box(this).supplier; }
                }
                """, 17);
    }

    @Test
    public void instanceMethodLambdaKeepsItsExplicitObjectCapture() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    private int value = 10;
                    private class Box {
                        java.util.function.IntSupplier make(SyntheticOuter other) { return () -> other.value + 7; }
                    }
                    public java.util.function.IntSupplier reader(int captured) { return new Box().make(this); }
                }
                """, 17);
    }

    @Test
    public void ordinaryConstructorFieldAssignmentsRemainVisible() throws Exception {
        assertRoundTrip("""
                public class SyntheticOuter {
                    private int value;
                    public SyntheticOuter() { this(9); }
                    private SyntheticOuter(int number) { this.value = number + 1; }
                    public java.util.function.IntSupplier reader(int captured) { return () -> value + 7; }
                }
                """, 17);
    }

    private static void assertRoundTrip(String source, int expected) throws Exception {
        assertRoundTrip(source, expected, "8");
    }

    private static void assertRoundTrip(String source, int expected, String release) throws Exception {
        assertRoundTrip(source, expected, release, false);
    }

    private static void assertRoundTrip(String source, int expected, String release, boolean legacy) throws Exception {
        Path original = compile(source, release);
        if (legacy) {
            rewriteLegacyCapture(original);
        }
        assertRoundTrip(original, expected, release);
    }

    private static void assertRoundTrip(Path original, int expected, String release) throws Exception {
        Loader loader = new Loader() {
            private final Loader fallback = new ClassPathLoader();

            @Override
            public boolean canLoad(String name) {
                return Files.isRegularFile(original.resolve(name.endsWith(".class") ? name : name + ".class")) || fallback.canLoad(name);
            }

            @Override
            public byte[] load(String name) throws IOException {
                Path file = original.resolve(name.endsWith(".class") ? name : name + ".class");
                return Files.isRegularFile(file) ? Files.readAllBytes(file) : fallback.load(name);
            }
        };
        Preferences preferences = new Preferences();
        preferences.setShowLineNumbers(false);
        String output = new PrinterImpl(preferences).buildDecompiledOutput(
                loader, "SyntheticOuter", preferences, new DecompilerImpl());
        assertFalse(output, output.contains("this$"));
        assertFalse(output, output.contains("access$"));
        assertFalse(output, output.contains("val$"));
        assertFalse(output, output.contains("$SwitchMap$"));
        assertFalse(output, output.contains("$assertionsDisabled"));
        Path rebuilt = compile(output, release);
        assertEquals(expected, evaluate(original, true));
        assertEquals(expected, evaluate(rebuilt, true));
        assertEquals(evaluate(original, false), evaluate(rebuilt, false));
    }

    private static Path compile(String source, String release) throws IOException {
        Path directory = Files.createTempDirectory(Path.of("target"), "synthetic-outer-");
        var compiler = ToolProvider.getSystemJavaCompiler();
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///SyntheticOuter.java"),
                JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return source;
            }
        };
        try (var manager = compiler.getStandardFileManager(diagnostics, null, null)) {
            boolean compiled = compiler.getTask(null, manager, diagnostics,
                    List.of("--release", release, "-g", "-proc:none", "-d", directory.toString()),
                    null, List.of(file)).call();
            assertTrue(source + "\n" + diagnostics.getDiagnostics(), compiled);
        }
        return directory;
    }

    private static void removeUnusedOuterField(Path file) throws Exception {
        ClassGen generated = new ClassGen(new ClassParser(file.toString()).parse());
        for (var field : generated.getFields()) {
            if (field.getName().startsWith("this$")) {
                generated.removeField(field);
            }
        }
        for (var method : generated.getMethods()) {
            if (!"<init>".equals(method.getName())) {
                continue;
            }
            MethodGen constructor = new MethodGen(method, generated.getClassName(), generated.getConstantPool());
            var instructions = constructor.getInstructionList();
            var end = instructions.getStart().getNext().getNext();
            var next = end.getNext();
            try {
                instructions.delete(instructions.getStart(), end);
            } catch (org.apache.bcel.generic.TargetLostException targets) {
                for (var target : targets.getTargets()) {
                    for (var user : target.getTargeters()) {
                        user.updateTarget(target, next);
                    }
                }
            }
            generated.replaceMethod(method, constructor.getMethod());
        }
        generated.getJavaClass().dump(file.toString());
    }

    private static void rewriteLegacyCapture(Path directory) throws IOException {
        Path file = directory.resolve("SyntheticOuter$1.class");
        ClassGen generated = new ClassGen(new ClassParser(file.toString()).parse());
        generated.setMajor(Const.MAJOR_1_4);
        for (var field : generated.getFields()) {
            field.setAccessFlags(field.getAccessFlags() & ~Const.ACC_SYNTHETIC);
        }
        for (var method : generated.getMethods()) {
            if (!"<init>".equals(method.getName())) {
                continue;
            }
            MethodGen constructor = new MethodGen(method, generated.getClassName(), generated.getConstantPool());
            var instructions = constructor.getInstructionList();
            for (var handle : instructions) {
                if (handle.getInstruction() instanceof INVOKESPECIAL) {
                    // Recreate javac 1.4: call super(), then initialize the unflagged capture.
                    instructions.move(instructions.getStart(), handle.getPrev().getPrev(), handle);
                    break;
                }
            }
            generated.replaceMethod(method, constructor.getMethod());
        }
        generated.getJavaClass().dump(file.toString());
    }

    private static int evaluate(Path directory, boolean assertionsEnabled) throws Exception {
        try (var loader = new URLClassLoader(new java.net.URL[] { directory.toUri().toURL() },
                ClassLoader.getPlatformClassLoader())) {
            loader.setDefaultAssertionStatus(assertionsEnabled);
            Class<?> type = loader.loadClass("SyntheticOuter");
            Object instance = type.getConstructor().newInstance();
            IntSupplier supplier = (IntSupplier) type.getMethod("reader", int.class).invoke(instance, 7);
            return supplier.getAsInt();
        }
    }
}
