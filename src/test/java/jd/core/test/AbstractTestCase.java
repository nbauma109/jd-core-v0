package jd.core.test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.fail;

import jd.core.preferences.Preferences;
import jd.core.printer.PrinterImpl;
import jd.core.process.DecompilerImpl;

public abstract class AbstractTestCase {

    private static final String DEFAULT_JDK_VERSION = JavaCore.VERSION_1_8;

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
        if (recompile()) {
            ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            parser.setSource(decompiledOutput.toCharArray());
            parser.setResolveBindings(true);
            parser.setBindingsRecovery(true);
            parser.setStatementsRecovery(true);
            String[] classpathEntries = System.getProperty("java.class.path").split(File.pathSeparator);
            parser.setEnvironment(classpathEntries, null, null, true);
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
