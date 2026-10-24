package jd.core.test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;

import jd.core.preferences.Preferences;
import jd.core.printer.PrinterImpl;
import jd.core.process.DecompilerImpl;

/** Standalone worker for scripts/audit-synthetic-jars.py; never scans the local repository in CI. */
public final class SyntheticJarAudit {
    private static final Pattern SYNTHETIC_NAME = Pattern.compile(
            "(?:access\\$\\d+|this\\$\\d+|val\\$[\\w$]+|\\$SwitchMap\\$[\\w$]+"
            + "|\\$SWITCH_TABLE\\$[\\w$]+|lambda\\$[\\w$]+|\\$assertionsDisabled)");

    private SyntheticJarAudit() {
    }

    public static void main(String[] args) throws Exception {
        Path jarPath = Path.of(args[0]);
        Path output = Path.of(args[1]);
        Files.createDirectories(output);
        int scanned = 0;
        int findings = 0;
        int errors = 0;
        try (JarFile jar = new JarFile(jarPath.toFile());
                var input = Files.newInputStream(jarPath);
                var report = Files.newBufferedWriter(output.resolve("findings.tsv"))) {
            CompositeLoader loader = new CompositeLoader(input);
            Set<String> candidates = new TreeSet<>();
            int bytecodeClasses = 0;
            for (var entry : Collections.list(jar.entries())) {
                String name = entry.getName();
                if (!name.endsWith(".class") || name.startsWith("META-INF/")) {
                    continue;
                }
                bytecodeClasses++;
                try (var bytes = jar.getInputStream(entry)) {
                    // Include a top-level class when any of its nested classes contains a candidate.
                    if (SYNTHETIC_NAME.matcher(new String(bytes.readAllBytes(), StandardCharsets.ISO_8859_1)).find()) {
                        candidates.add(name.substring(0, name.length() - 6).split("\\$", 2)[0]);
                    }
                }
            }
            if (args.length > 2) {
                candidates = new TreeSet<>(Files.readAllLines(Path.of(args[2])));
            }
            for (String name : candidates) {
                if (jar.getJarEntry(name + ".class") == null) {
                    continue;
                }
                System.out.println("CLASS\t" + name);
                scanned++;
                try {
                    Preferences preferences = new Preferences();
                    preferences.setShowLineNumbers(false);
                    String source = new PrinterImpl(preferences).buildDecompiledOutput(
                            loader, name, preferences, new DecompilerImpl());
                    Set<String> identifiers = syntheticIdentifiers(source);
                    if (!identifiers.isEmpty()) {
                        findings++;
                        report.write("CANDIDATE\t" + name + "\t" + identifiers + "\n");
                        Files.writeString(output.resolve(name.replace('/', '_') + ".java"), source);
                    }
                    if (source.contains("// Byte code:")) {
                        errors++;
                        report.write("BYTECODE_FALLBACK\t" + name + "\n");
                    }
                } catch (Exception | AssertionError | StackOverflowError failure) {
                    errors++;
                    report.write("ERROR\t" + name + "\t" + failure + "\n");
                }
                report.flush();
            }
            System.out.println("TOTAL\tbytecodeClasses=" + bytecodeClasses + "\tdecompiled=" + scanned
                    + "\tcandidates=" + findings + "\terrors=" + errors);
        }
    }

    static Set<String> syntheticIdentifiers(String source) throws InvalidInputException {
        Set<String> names = new HashSet<>();
        var scanner = ToolFactory.createScanner(false, false, false, "17", "17");
        scanner.setSource(source.toCharArray());
        int token;
        while ((token = scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
            if (token == ITerminalSymbols.TokenNameIdentifier) {
                String identifier = new String(scanner.getCurrentTokenSource());
                if (SYNTHETIC_NAME.matcher(identifier).matches()) {
                    names.add(identifier);
                }
            }
        }
        return new TreeSet<>(names);
    }
}
