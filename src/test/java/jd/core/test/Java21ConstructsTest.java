package jd.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.jd.core.v1.api.loader.Loader;
import org.junit.Assume;
import org.junit.Test;

import jd.core.preferences.Preferences;
import jd.core.printer.PrinterImpl;
import jd.core.process.DecompilerImpl;

/**
 * Compiles sources with the javac of the running JDK, decompiles them, compiles the result again
 * and checks that both versions behave the same.
 */
public class Java21ConstructsTest {

    private static final String SOURCE = """
            import java.io.IOException;

            public class Constructs {
                sealed interface Shape permits Circle, Square {}
                record Circle(double r) implements Shape {}
                record Square(double s) implements Shape {}
                record Pair(int left, Object right) {}
                enum Mode { ON, OFF }

                static final StringBuilder LOG = new StringBuilder();

                static class Resource implements AutoCloseable {
                    private final String name;
                    Resource(String name) { this.name = name; LOG.append("open ").append(name).append(';'); }
                    @Override public void close() { LOG.append("close ").append(name).append(';'); }
                }

                static void accept(Object o, boolean b) { LOG.append(b).append(';'); }

                public String instanceOf(Object o) {
                    if (o instanceof String s && s.length() > 2) {
                        return "long " + s;
                    }
                    if (!(o instanceof Integer i)) {
                        return "other";
                    }
                    return "int " + (i + 1);
                }

                public String instanceOfInArgument(Object o) {
                    LOG.setLength(0);
                    accept(o, o instanceof String s && s.length() > 5);
                    return LOG.toString();
                }

                public boolean instanceOfReturned(Object o) {
                    return o instanceof String s && s.isEmpty();
                }

                public String instanceOfTernary(Object o) {
                    return o instanceof Integer i ? "i" + i : "n";
                }

                public String recordPattern(Object o) {
                    if (o instanceof Pair(int left, Object right)) {
                        return left + ":" + right;
                    }
                    return "none";
                }

                public String switchOnTypes(Object o) {
                    return switch (o) {
                        case null -> "null";
                        case Integer i when i > 5 -> "big " + i;
                        case Integer i -> "small " + i;
                        case String s -> s;
                        default -> "other";
                    };
                }

                public String switchStatement(Object o) {
                    switch (o) {
                        case Integer i:
                            return "int " + i;
                        case String s:
                            return "str " + s;
                        default:
                            return "other";
                    }
                }

                public double switchOnSealed(Shape shape) {
                    return switch (shape) {
                        case Circle c -> c.r();
                        case Square q -> q.s() * 2;
                    };
                }

                public String switchOnRecords(Shape shape) {
                    return switch (shape) {
                        case Circle(double r) -> "circle " + r;
                        case Square(double s) -> "square " + s;
                    };
                }

                public String localTypes(int i) {
                    enum Kind { SMALL, LARGE }
                    record Box(int value) {}
                    Kind kind = i < 10 ? Kind.SMALL : Kind.LARGE;
                    return kind + " " + new Box(i);
                }

                public String tryWithResources(boolean fail) {
                    LOG.setLength(0);
                    try (Resource first = new Resource("a"); Resource second = new Resource("b")) {
                        LOG.append("body;");
                        if (fail) {
                            throw new IllegalStateException();
                        }
                    } catch (IllegalStateException e) {
                        LOG.append("caught;");
                    } finally {
                        LOG.append("finally;");
                    }
                    return LOG.toString();
                }

                public String tryWithResourcesReturning() throws IOException {
                    LOG.setLength(0);
                    try (Resource only = new Resource("c")) {
                        return LOG.append("return;").toString();
                    }
                }
            }
            """;

    @Test
    public void behaviorIsPreserved() throws Exception {
        Assume.assumeTrue(Runtime.version().feature() >= 21);
        Path original = compile(SOURCE, "Constructs");
        String decompiled = decompile(original);

        assertFalse(decompiled, decompiled.contains("Byte code"));
        assertFalse(decompiled, decompiled.contains("addSuppressed"));
        assertFalse(decompiled, decompiled.contains("MatchException"));
        assertTrue(decompiled, decompiled.contains("instanceof String"));
        assertTrue(decompiled, java.util.regex.Pattern.compile("instanceof [\\w.]*Pair\\(").matcher(decompiled).find());
        assertTrue(decompiled, java.util.regex.Pattern.compile("case [\\w.]*Circle\\(").matcher(decompiled).find());
        assertTrue(decompiled, decompiled.contains(" when "));
        assertTrue(decompiled, decompiled.contains("try ("));

        Path rebuilt = compile(decompiled, "Constructs");
        try (URLClassLoader first = new URLClassLoader(new java.net.URL[] { original.toUri().toURL() }, null);
                URLClassLoader second = new URLClassLoader(new java.net.URL[] { rebuilt.toUri().toURL() }, null)) {
            for (Object argument : new Object[] { "ab", "abcdef", "", 1, 7, 2.5, null }) {
                assertSame(first, second, "instanceOf", argument);
                assertSame(first, second, "instanceOfInArgument", argument);
                assertSame(first, second, "instanceOfReturned", argument);
                assertSame(first, second, "instanceOfTernary", argument);
                assertSame(first, second, "switchOnTypes", argument);
                assertSame(first, second, "switchStatement", argument);
            }
            assertSame(first, second, "localTypes", 3);
            assertSame(first, second, "localTypes", 30);
            assertSame(first, second, "tryWithResources", true);
            assertSame(first, second, "tryWithResources", false);
            assertSame(first, second, "tryWithResourcesReturning");
            assertEquals(
                    invokeWithShapes(first, "switchOnSealed", "Circle", 1.5),
                    invokeWithShapes(second, "switchOnSealed", "Circle", 1.5));
            assertEquals(
                    invokeWithShapes(first, "switchOnRecords", "Square", 2.0),
                    invokeWithShapes(second, "switchOnRecords", "Square", 2.0));
        }
    }

    private static Object invokeWithShapes(ClassLoader loader, String method, String shape, double value) throws Exception {
        Class<?> constructs = loader.loadClass("Constructs");
        Class<?> shapeType = loader.loadClass("Constructs$" + shape);
        var constructor = shapeType.getDeclaredConstructor(double.class);
        constructor.setAccessible(true);
        Object argument = constructor.newInstance(value);
        var target = constructs.getMethod(method, loader.loadClass("Constructs$Shape"));
        return target.invoke(constructs.getConstructor().newInstance(), argument);
    }

    private static void assertSame(ClassLoader first, ClassLoader second, String method, Object... arguments) throws Exception {
        assertEquals(method, invoke(first, method, arguments), invoke(second, method, arguments));
    }

    private static Object invoke(ClassLoader loader, String name, Object... arguments) throws Exception {
        Class<?> constructs = loader.loadClass("Constructs");
        for (var method : constructs.getMethods()) {
            if (method.getName().equals(name)) {
                try {
                    return method.invoke(constructs.getConstructor().newInstance(), arguments);
                } catch (InvocationTargetException e) {
                    return e.getCause().getClass().getName();
                }
            }
        }
        throw new IllegalArgumentException(name);
    }

    private static String decompile(Path directory) throws IOException {
        Loader loader = new Loader() {
            @Override
            public boolean canLoad(String name) {
                return name.endsWith(".class") && Files.isRegularFile(directory.resolve(name));
            }

            @Override
            public byte[] load(String name) throws IOException {
                return Files.readAllBytes(directory.resolve(name));
            }
        };
        Preferences preferences = new Preferences();
        preferences.setShowLineNumbers(false);
        return new PrinterImpl(preferences).buildDecompiledOutput(
                loader, "Constructs", preferences, new DecompilerImpl());
    }

    private static Path compile(String source, String name) throws IOException {
        Path directory = Files.createTempDirectory(Path.of("target"), "constructs-");
        var compiler = ToolProvider.getSystemJavaCompiler();
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        JavaFileObject file = new SimpleJavaFileObject(URI.create("string:///" + name + ".java"),
                JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return source;
            }
        };
        try (var manager = compiler.getStandardFileManager(diagnostics, null, null)) {
            boolean compiled = compiler.getTask(null, manager, diagnostics,
                    List.of("--release", "21", "-g", "-proc:none", "-nowarn", "-d", directory.toString()),
                    null, List.of(file)).call();
            assertTrue(source + "\n" + diagnostics.getDiagnostics(), compiled);
        }
        return directory;
    }
}
