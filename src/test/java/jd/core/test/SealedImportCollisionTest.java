package jd.core.test;

import org.jd.core.v1.api.loader.Loader;
import org.junit.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SealedImportCollisionTest extends AbstractTestCase {
    @Test
    public void testPermittedTypesWithCollidingNames() throws Exception {
        Path sourceRoot = Files.createTempDirectory(Path.of("target"), "sealed-module-src-");
        Path classes = Files.createTempDirectory(Path.of("target"), "sealed-module-classes-");
        Path module = writeSource(sourceRoot, "module-info.java", "module sample.sealedtypes {}\n");
        Path parent = writeSource(sourceRoot, "base/Foo.java", "package base; public class Foo {}\n");
        Path sealed = writeSource(sourceRoot, "a/Shape.java",
            "package a; public sealed class Shape extends base.Foo permits b.Shape, c.Shape, impl.Foo { "
                + "public d.Shape make() { return new d.Shape(); } }\n");
        Path first = writeSource(sourceRoot, "b/Shape.java",
            "package b; public final class Shape extends a.Shape {}\n");
        Path second = writeSource(sourceRoot, "c/Shape.java",
            "package c; public final class Shape extends a.Shape {}\n");
        Path third = writeSource(sourceRoot, "impl/Foo.java",
            "package impl; public final class Foo extends a.Shape {}\n");
        Path unrelated = writeSource(sourceRoot, "d/Shape.java",
            "package d; public class Shape {}\n");

        Loader loader = compile(classes, module, parent, sealed, first, second, third, unrelated);

        String output = decompile("a/Shape", loader, "17");
        assertTrue(output, output.replaceAll("\\s+", " ").contains(
            "sealed class Shape extends base.Foo permits b.Shape, c.Shape, impl.Foo"));
        assertTrue(output, !output.contains("import b.Shape;") && !output.contains("import c.Shape;")
            && !output.contains("import d.Shape;") && !output.contains("import impl.Foo;")
            && !output.contains("import base.Foo;"));
        assertTrue(output, output.contains("d.Shape make()"));
    }

    @Test
    public void testDefaultPackagePermittedClass() throws Exception {
        Path sourceRoot = Files.createTempDirectory(Path.of("target"), "sealed-default-src-");
        Path classes = Files.createTempDirectory(Path.of("target"), "sealed-default-classes-");
        Path source = writeSource(sourceRoot, "DefaultShape.java",
            "public sealed class DefaultShape permits DefaultLeaf {} "
                + "final class DefaultLeaf extends DefaultShape {}\n");

        String output = decompile("DefaultShape", compile(classes, source), "17");
        assertTrue(output, output.replaceAll("\\s+", " ").contains(
            "sealed class DefaultShape permits DefaultLeaf"));
    }

    @Test
    public void testPermittedTypeDoesNotShadowJavaLang() throws Exception {
        Path sourceRoot = Files.createTempDirectory(Path.of("target"), "sealed-java-lang-src-");
        Path classes = Files.createTempDirectory(Path.of("target"), "sealed-java-lang-classes-");
        Path module = writeSource(sourceRoot, "module-info.java", "module sample.sealedtypes {}\n");
        Path sealed = writeSource(sourceRoot, "a/TextShape.java",
            "package a; public sealed class TextShape permits other.String { "
                + "public java.lang.String text() { return \"text\"; } }\n");
        Path permitted = writeSource(sourceRoot, "other/String.java",
            "package other; public final class String extends a.TextShape {}\n");

        String output = decompile("a/TextShape", compile(classes, module, sealed, permitted), "17");
        assertTrue(output, output.contains("permits other.String"));
        assertTrue(output, !output.contains("import other.String;"));
        assertTrue(output, output.contains("String text()"));
    }

    @Test
    public void testDefaultPackageGenericSealedClass() throws Exception {
        Path sourceRoot = Files.createTempDirectory(Path.of("target"), "sealed-default-generic-src-");
        Path classes = Files.createTempDirectory(Path.of("target"), "sealed-default-generic-classes-");
        Path source = writeSource(sourceRoot, "GenericShape.java",
            "public sealed class GenericShape<T> permits GenericLeaf {} "
                + "final class GenericLeaf extends GenericShape<String> {}\n");

        Loader loader = compile(classes, source);
        String baseOutput = decompile("GenericShape", loader, "17");
        String leafOutput = decompile("GenericLeaf", loader, "17");
        assertTrue(baseOutput, baseOutput.replaceAll("\\s+", " ").contains(
            "sealed class GenericShape<T> permits GenericLeaf"));
        assertTrue(leafOutput, leafOutput.contains("extends GenericShape<String>"));
    }

    private static Loader compile(Path classes, Path... sources) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler);
        List<String> arguments = new ArrayList<>(List.of("--release", "17", "-d", classes.toString()));
        for (Path source : sources) {
            arguments.add(source.toString());
        }
        assertEquals(0, compiler.run(null, null, null, arguments.toArray(String[]::new)));

        Map<String, byte[]> classFiles = new HashMap<>();
        try (Stream<Path> paths = Files.walk(classes)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                classFiles.put(classes.relativize(path).toString().replace('\\', '/'),
                    Files.readAllBytes(path));
            }
        }

        return new Loader() {
            @Override
            public boolean canLoad(String internalName) {
                return classFiles.containsKey(internalName);
            }

            @Override
            public byte[] load(String internalName) throws IOException {
                return classFiles.get(internalName);
            }
        };
    }

    private static Path writeSource(Path root, String name, String source) throws IOException {
        Path path = root.resolve(name);
        Files.createDirectories(path.getParent());
        return Files.writeString(path, source);
    }

    @Override
    protected boolean recompile() {
        // AbstractTestCase recompiles on the classpath, without these generated types.
        return false;
    }
}
