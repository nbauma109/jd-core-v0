package jd.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.tools.ToolProvider;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Constructs found while auditing JARs, which failed the decompilation of their whole class. */
public class ClassCrashTest extends AbstractTestCase {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testAnnotatedParametersAfterParametersAddedByTheCompiler() throws Exception {
        compile("Annotated", """
                import java.lang.annotation.*;

                public class Annotated {
                    @Retention(RetentionPolicy.RUNTIME) @interface Kept {}
                    @Retention(RetentionPolicy.CLASS) @interface Dropped {}

                    class Inner {
                        Inner(@Kept String name, @Dropped int size) {
                            System.out.println(name + size);
                        }
                    }

                    enum Color {
                        RED("r", 1);

                        Color(@Kept String name, @Dropped int size) {
                            System.out.println(name + size);
                        }
                    }
                }
                """);

        String output = decompile("Annotated", loader(Files.readAllBytes(classFile("Annotated"))));

        assertTrue(output, output.contains("Inner(@Annotated.Kept String name, @Annotated.Dropped int size)"));
        assertTrue(output, output.contains("Color(@Annotated.Kept String name, @Annotated.Dropped int size)"));
    }

    /** Kotlin declares synthetic some classes whose methods are written in the source. */
    @Test
    public void testSyntheticClassWithMethodsOfTheSource() throws Exception {
        compile("Facade", """
                public class Facade {
                    public static String greet(String name) {
                        return "Hello " + name;
                    }
                }
                """);
        JavaClass facade = new ClassParser(classFile("Facade").toString()).parse();
        facade.setAccessFlags(facade.getAccessFlags() | Const.ACC_SYNTHETIC);

        String output = decompile("Facade", loader(facade.getBytes()));

        assertTrue(output, output.contains("return \"Hello \" + name;"));
    }

    @Override
    protected boolean recompile() {
        // The classes are compiled in a temporary folder, which the compiler of the check does not know
        return false;
    }

    private void compile(String name, String source) throws IOException {
        Path file = folder.getRoot().toPath().resolve(name + ".java");
        Files.writeString(file, source);
        int status = ToolProvider.getSystemJavaCompiler().run(null, null, null, "--release", "17", "-g",
                file.toString());
        assertEquals(0, status);
    }

    private Path classFile(String name) {
        return folder.getRoot().toPath().resolve(name + ".class");
    }

    /** Loads the main class from the bytes, its nested classes from the folder, and the others from the class path. */
    private Loader loader(byte[] main) {
        ClassPathLoader classPathLoader = new ClassPathLoader();
        return new Loader() {
            @Override
            public boolean canLoad(String name) {
                return Files.exists(classFile(internalName(name))) || classPathLoader.canLoad(name);
            }

            @Override
            public byte[] load(String name) throws IOException {
                String internalName = internalName(name);
                if (!Files.exists(classFile(internalName))) {
                    return classPathLoader.load(name);
                }
                return internalName.contains("$") ? Files.readAllBytes(classFile(internalName)) : main;
            }

            private String internalName(String name) {
                return name.replace(".class", "");
            }
        };
    }
}
