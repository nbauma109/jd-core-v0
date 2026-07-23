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

import static org.junit.Assert.fail;

import jd.core.preferences.Preferences;
import jd.core.printer.PrinterImpl;
import jd.core.process.DecompilerImpl;

public abstract class AbstractTestCase {

    private static final String DEFAULT_JDK_VERSION = JavaCore.VERSION_1_8;

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
        {
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
            boolean incompleteClasspath = false;
            for (IProblem problem : unit.getProblems()) {
                if (problem.isError()) {
                    String message = problem.getMessage();
                    incompleteClasspath |= message.startsWith("The import ")
                            && message.endsWith(" cannot be resolved")
                            || message.contains("indirectly referenced from required type");
                    sb.append(System.lineSeparator());
                    sb.append('L');
                    sb.append(problem.getSourceLineNumber());
                    sb.append(": ");
                    sb.append(problem.getMessage());
                }
            }
            if (!incompleteClasspath && !sb.isEmpty()) {
                System.out.println(decompiledOutput);
                fail(sb.toString());
            }
        }
        return decompiledOutput;
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
