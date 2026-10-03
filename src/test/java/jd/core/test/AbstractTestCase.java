package jd.core.test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.jd.core.v1.util.ZipLoader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.fail;

import jd.core.preferences.Preferences;
import jd.core.printer.PrinterImpl;
import jd.core.process.DecompilerImpl;

public abstract class AbstractTestCase {

    private static final String DEFAULT_JDK_VERSION = JavaCore.VERSION_1_8;
    private static final Pattern SOURCE_LINE_PREFIX = Pattern.compile("^/\\*\\s*(\\d+)\\s*\\*/");

    protected URL expectedResource(String name) {
        return expectedResource(getClass(), name);
    }

    static URL expectedResource(Class<?> context, String name) {
        if ("javac".equals(System.getProperty("test.compiler", "javac"))) {
            int extension = name.lastIndexOf('.');
            String javacName = extension == -1
                    ? name + "Javac"
                    : name.substring(0, extension) + "Javac" + name.substring(extension);
            URL resource = context.getResource(javacName);
            if (resource != null) {
                return resource;
            }
        }
        return context.getResource(name);
    }

    protected String decompile(String internalTypeName, Loader loader, String jdkVersion) throws IOException {

        DecompilerImpl decompiler = new DecompilerImpl();

        Preferences preferences = new Preferences();
        preferences.setRealignmentLineNumber(realignmentLineNumber());
        preferences.setShowDefaultConstructor(showDefaultConstructor());
        preferences.setShowLineNumbers(showLineNumbers());
        preferences.setShowPrefixThis(true);
        preferences.setUnicodeEscape(true);
        preferences.setWriteMetaData(showMetaData());

        PrinterImpl printer = new PrinterImpl(preferences);

        String decompiledOutput = printer.buildDecompiledOutput(loader, internalTypeName, preferences, decompiler);
        if (preferences.getRealignmentLineNumber() && preferences.isShowLineNumbers()) {
            assertRealignedLineNumbers(internalTypeName, decompiledOutput);
        }
        if (recompile()) {
            ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            parser.setSource(decompiledOutput.toCharArray());
            parser.setResolveBindings(true);
            parser.setBindingsRecovery(true);
            parser.setStatementsRecovery(true);
            parser.setEnvironment(classpathEntries(loader), null, null, true);
            parser.setUnitName(internalTypeName + ".java");
    
            Map<String, String> options = JavaCore.getOptions();
            options.put(JavaCore.CORE_ENCODING, StandardCharsets.UTF_8.name());
            options.put(JavaCore.COMPILER_COMPLIANCE, jdkVersion);
            options.put(JavaCore.COMPILER_SOURCE, jdkVersion);
            parser.setCompilerOptions(options);

            StringBuilder sb = new StringBuilder();
            CompilationUnit unit = (CompilationUnit) parser.createAST(null);
            for (IProblem problem : unit.getProblems()) {
                if (problem.isError()) {
                    String message = problem.getMessage();
                    boolean classpathError = message.startsWith("The import ")
                                    && message.endsWith(" cannot be resolved")
                            || message.contains("indirectly referenced from required type");
                    if (classpathError) {
                        continue;
                    }
                    sb.append(System.lineSeparator());
                    sb.append('L');
                    sb.append(problem.getSourceLineNumber());
                    sb.append(": ");
                    sb.append(problem.getMessage());
                }
            }
            if (!sb.isEmpty()) {
                System.out.println(decompiledOutput);
                fail(sb.toString());
            }
        }
        return decompiledOutput;
    }

    static void assertRealignedLineNumbers(String internalTypeName, String output) {
        StringBuilder mismatches = new StringBuilder();
        // Java source lines are delimited by CR/LF. \R also treats literal
        // Unicode line separators inside character constants as new lines.
        String[] lines = output.split("\\r\\n|\\n|\\r", -1);
        for (int index = 0; index < lines.length; index++) {
            Matcher match = SOURCE_LINE_PREFIX.matcher(lines[index]);
            if (match.find() && Integer.parseInt(match.group(1)) != index + 1
                    && mismatches.length() < 1000) {
                mismatches.append("\nphysical line ").append(index + 1)
                        .append(" has source line ").append(match.group(1));
            }
        }
        if (!mismatches.isEmpty()) {
            fail(internalTypeName + " has misaligned line numbers:" + mismatches);
        }
    }

    private static String[] classpathEntries(Loader loader) throws IOException {
        List<String> entries = new ArrayList<>(List.of(
                System.getProperty("java.class.path").split(File.pathSeparator)));
        entries.removeIf(entry -> !Files.exists(Path.of(entry)));
        Map<String, byte[]> classes = null;
        if (loader instanceof ZipLoader zipLoader) {
            classes = zipLoader.getMap();
        } else if (loader instanceof CompositeLoader compositeLoader) {
            classes = compositeLoader.getMap();
        }
        if (classes != null) {
            Path directory = Files.createTempDirectory(Path.of("target"), "recompile-classpath-");
            for (Map.Entry<String, byte[]> entry : classes.entrySet()) {
                String fileName = entry.getKey();
                byte[] content = entry.getValue();
                if (!fileName.endsWith(".class")
                        && content.length >= 4
                        && content[0] == (byte) 0xCA
                        && content[1] == (byte) 0xFE
                        && content[2] == (byte) 0xBA
                        && content[3] == (byte) 0xBE) {
                    fileName += ".class";
                }
                Path classFile = directory.resolve(fileName);
                Files.createDirectories(classFile.getParent());
                Files.write(classFile, content);
            }
            entries.add(0, directory.toString());
        }
        return entries.toArray(String[]::new);
    }

    protected boolean showLineNumbers() {
        return true;
    }

    protected boolean recompile() {
        return true;
    }

    protected String decompile(String internalTypeName) throws IOException {
        return decompile(internalTypeName, DEFAULT_JDK_VERSION);
    }
    
    protected String decompile(String internalTypeName, Loader loader) throws IOException {
        return decompile(internalTypeName, loader, DEFAULT_JDK_VERSION);
    }
    
    protected String decompile(String internalTypeName, String jdkVersion) throws IOException {
        return decompile(internalTypeName, new ClassPathLoader(), jdkVersion);
    }
    
    protected boolean showDefaultConstructor() {
        return false;
    }
    
    protected boolean showMetaData() {
        return false;
    }
    
    protected boolean realignmentLineNumber() {
        return true;
    }
}
