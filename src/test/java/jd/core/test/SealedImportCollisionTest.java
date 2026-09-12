package jd.core.test;

import org.jd.core.v1.api.loader.Loader;
import org.junit.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
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
            "package a; public sealed class Shape extends base.Foo permits b.Shape, c.Shape, impl.Foo {}\n");
        Path first = writeSource(sourceRoot, "b/Shape.java",
            "package b; public final class Shape extends a.Shape {}\n");
        Path second = writeSource(sourceRoot, "c/Shape.java",
            "package c; public final class Shape extends a.Shape {}\n");
        Path third = writeSource(sourceRoot, "impl/Foo.java",
            "package impl; public final class Foo extends a.Shape {}\n");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler);
        assertEquals(0, compiler.run(null, null, null, "--release", "17", "-d", classes.toString(),
            module.toString(), parent.toString(), sealed.toString(), first.toString(),
            second.toString(), third.toString()));

        Map<String, byte[]> classFiles = new HashMap<>();
        try (Stream<Path> paths = Files.walk(classes)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                classFiles.put(classes.relativize(path).toString().replace('\\', '/'),
                    Files.readAllBytes(path));
            }
        }

        Loader loader = new Loader() {
            @Override
            public boolean canLoad(String internalName) {
                return classFiles.containsKey(internalName);
            }

            @Override
            public byte[] load(String internalName) throws IOException {
                return classFiles.get(internalName);
            }
        };

        String output = decompile("a/Shape", loader, "17");
        assertTrue(output, output.replaceAll("\\s+", " ").contains(
            "sealed class Shape extends base.Foo permits b.Shape, c.Shape, impl.Foo"));
        assertTrue(output, !output.contains("import b.Shape;") && !output.contains("import c.Shape;")
            && !output.contains("import impl.Foo;") && !output.contains("import base.Foo;"));
    }

    private static Path writeSource(Path root, String name, String source) throws IOException {
        Path path = root.resolve(name);
        Files.createDirectories(path.getParent());
        return Files.writeString(path, source);
    }

    @Override
    protected boolean recompile() {
        // This fixture is a named module; AbstractTestCase recompiles on the classpath.
        return false;
    }
}
