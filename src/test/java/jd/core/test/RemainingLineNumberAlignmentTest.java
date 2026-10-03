package jd.core.test;

import jd.core.preferences.Preferences;
import jd.core.printer.PrinterImpl;
import jd.core.process.DecompilerImpl;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Test;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RemainingLineNumberAlignmentTest {
    private String decompile(String name) throws Exception {
        Preferences preferences = new Preferences();
        preferences.setRealignmentLineNumber(true);
        preferences.setShowLineNumbers(true);
        preferences.setWriteMetaData(false);
        PrinterImpl printer = new PrinterImpl(preferences);
        String output = printer.buildDecompiledOutput(new ClassPathLoader(),
                "jd/core/test/" + name, preferences, new DecompilerImpl());
        AbstractTestCase.assertRealignedLineNumbers(name, output);
        assertEquals(output, 0, printer.getSuppressedLineNumberCount());
        return output;
    }

    @Test
    public void loopBackEdgeDoesNotExpandConditionIntoBody() throws Exception {
        String output = decompile("LoopConditionLineNumbers");
        String[] lines = output.split("\\r\\n|\\n|\\r", -1);
        assertTrue(output, lines[11].startsWith("/* 12 */") && lines[11].contains("return current;"));
        assertTrue(output, lines[13].startsWith("/* 14 */") && lines[13].contains("return current.parent;"));
        assertTrue(output, lines[16].startsWith("/* 17 */") && lines[16].contains("return null;"));
        assertTrue(output, lines[20].startsWith("/* 21 */") && lines[20].contains("return 42;"));
    }

    @Test
    public void privateGenericRecordDoesNotPrintGeneratedMethods() throws Exception {
        String output = decompile("GenericRecordLineNumbers");
        assertFalse(output, output.contains("this.values ="));
        assertFalse(output, output.contains("this.index ="));
        assertFalse(output, output.contains("return this.values;"));
        assertFalse(output, output.contains(" index()"));
        assertTrue(output, output.contains("List<String> values"));
        assertTrue(output, output.contains("public VisibleConstructorCache"));
        assertTrue(output, output.contains("List.copyOf(this.values)"));
        String[] lines = output.split("\\r\\n|\\n|\\r", -1);
        assertTrue(output, lines[9].startsWith("/* 10 */") && lines[9].contains("return this.values.size();"));
        assertTrue(output, lines[14].startsWith("/* 15 */") && lines[14].contains("return 42;"));
        JavaFileObject source = new SimpleJavaFileObject(
                URI.create("string:///jd/core/test/GenericRecordLineNumbers.java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return output;
            }
        };
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        var compiler = ToolProvider.getSystemJavaCompiler();
        Path directory = Files.createTempDirectory("jd-record-alignment");
        try (var manager = compiler.getStandardFileManager(diagnostics, null, null)) {
            assertTrue(diagnostics.getDiagnostics().toString(), compiler.getTask(null, manager, diagnostics,
                    List.of("-proc:none", "--release", "17", "-d", directory.toString()), null,
                    List.of(source)).call());
        } finally {
            try (var paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
    }
}
