package jd.core.test;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.LineNumber;
import org.apache.bcel.classfile.LineNumberTable;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Assume;
import org.junit.Test;

import jd.core.preferences.Preferences;
import jd.core.printer.PrinterImpl;
import jd.core.process.DecompilerImpl;

import static org.junit.Assert.fail;

/** Run with -DlineAudit.dependencies=true to audit test dependencies or -DlineAudit.jar=PATH for a JAR. */
public class DependencyJarLineNumberAuditTest {
    private static final Pattern SOURCE_LINE_PREFIX = Pattern.compile("^/\\*\\s*(\\d+)\\s*\\*/\\s*(.*)");
    private static final Pattern BLANK_LINE_PREFIX = Pattern.compile("^/\\*\\s*\\*/\\s*(.*)");
    private static final List<String> TEST_DEPENDENCY_CLASSES = List.of(
            "org/apache/commons/io/IOUtils.class",
            "org/apache/commons/collections4/CollectionUtils.class",
            "net/sourceforge/plantuml/SourceStringReader.class",
            "org/eclipse/jdt/core/JavaCore.class",
            "org/junit/Test.class");

    @Test
    public void auditTestDependencyJars() throws Exception {
        Assume.assumeTrue(Boolean.getBoolean("lineAudit.dependencies")
                || System.getProperty("lineAudit.jar") != null);

        Preferences preferences = new Preferences();
        preferences.setRealignmentLineNumber(true);
        preferences.setShowLineNumbers(true);
        DecompilerImpl decompiler = new DecompilerImpl();
        StringBuilder mismatches = new StringBuilder();
        List<String> decompilationErrors = new ArrayList<>();
        List<String> suppressedExamples = new ArrayList<>();
        int scanned = 0;
        int decompiled = 0;
        int failedToDecompile = 0;
        int sourceLinesCompared = 0;
        int exactSourceLines = 0;
        int sourceLinesOutOfRange = 0;
        int classesWithoutSource = 0;
        int suppressedLineNumbers = 0;
        int blankLinesMatchingDebugSource = 0;
        List<String> blankDebugExamples = new ArrayList<>();

        String selectedMarker = System.getProperty("lineAudit.marker");
        String selectedClass = System.getProperty("lineAudit.class");
        String selectedJar = System.getProperty("lineAudit.jar");
        int start = Integer.getInteger("lineAudit.start", 0);
        int limit = Integer.getInteger("lineAudit.limit", Integer.MAX_VALUE);
        for (String marker : selectedJar == null ? TEST_DEPENDENCY_CLASSES : List.of(selectedJar)) {
            if (selectedMarker != null && !marker.equals(selectedMarker)) {
                continue;
            }
            Path jarPath;
            Loader loader;
            if (selectedJar == null) {
                URL resource = getClass().getClassLoader().getResource(marker);
                if (resource == null) {
                    fail("Missing test dependency class: " + marker);
                }
                JarURLConnection connection = (JarURLConnection) resource.openConnection();
                jarPath = Path.of(connection.getJarFileURL().toURI());
                loader = new ClassPathLoader();
            } else {
                jarPath = Path.of(selectedJar);
                try (var input = Files.newInputStream(jarPath)) {
                    loader = new CompositeLoader(input);
                }
            }
            String fileName = jarPath.getFileName().toString();
            Path sourcePath = jarPath.resolveSibling(fileName.substring(0, fileName.length() - 4) + "-sources.jar");
            if (!Files.isRegularFile(sourcePath)) {
                fail("Missing source JAR for " + jarPath);
            }
            int jarScanned = 0;
            int jarDecompiled = 0;
            int jarOrdinal = 0;
            Map<String, String[]> sourceCache = new HashMap<>();
            try (JarFile jar = new JarFile(jarPath.toFile()); JarFile sources = new JarFile(sourcePath.toFile())) {
                for (JarEntry entry : java.util.Collections.list(jar.entries())) {
                    String name = entry.getName();
                    if (!name.endsWith(".class") || name.startsWith("META-INF/")
                            || name.endsWith("module-info.class") || name.endsWith("package-info.class")) {
                        continue;
                    }
                    String internalName = name.substring(0, name.length() - ".class".length());
                    if (selectedClass != null && !internalName.equals(selectedClass)) {
                        continue;
                    }
                    jarOrdinal++;
                    if (jarOrdinal <= start || jarOrdinal - start > limit) {
                        continue;
                    }
                    scanned++;
                    jarScanned++;
                    try {
                        PrinterImpl printer = new PrinterImpl(preferences);
                        String output = printer.buildDecompiledOutput(loader, internalName, preferences, decompiler);
                        int suppressedForClass = printer.getSuppressedLineNumberCount();
                        suppressedLineNumbers += suppressedForClass;
                        if (suppressedForClass > 0 && suppressedExamples.size() < 20) {
                            suppressedExamples.add(internalName + ": " + suppressedForClass);
                        }
                        if (selectedClass != null) {
                            System.out.println(output);
                        }
                        decompiled++;
                        jarDecompiled++;
                        AbstractTestCase.assertRealignedLineNumbers(internalName, output);
                        String sourceName = internalName.split("\\$", 2)[0] + ".java";
                        String[] sourceLines = sourceCache.computeIfAbsent(sourceName,
                                key -> readSourceLines(sources, key));
                        if (sourceLines == null) {
                            classesWithoutSource++;
                        } else {
                            Set<Integer> debugLines = readDebugLines(jar, entry);
                            String[] outputLines = output.split("\\r\\n|\\n|\\r", -1);
                            for (int index = 0; index < outputLines.length; index++) {
                                String line = outputLines[index];
                                Matcher match = SOURCE_LINE_PREFIX.matcher(line);
                                if (!match.find()) {
                                    Matcher blank = BLANK_LINE_PREFIX.matcher(line);
                                    if (blank.find() && index < sourceLines.length && debugLines.contains(index + 1)
                                            && isStatementOrExpression(blank.group(1))
                                            && normalizeLine(blank.group(1)).equals(normalizeLine(sourceLines[index]))) {
                                        blankLinesMatchingDebugSource++;
                                        if (blankDebugExamples.size() < 20) {
                                            blankDebugExamples.add(internalName + ":" + (index + 1)
                                                    + " " + blank.group(1).trim());
                                        }
                                    }
                                    continue;
                                }
                                int sourceLine = Integer.parseInt(match.group(1));
                                if (sourceLine > sourceLines.length) {
                                    sourceLinesOutOfRange++;
                                    continue;
                                }
                                sourceLinesCompared++;
                                if (normalizeLine(match.group(2)).equals(normalizeLine(sourceLines[sourceLine - 1]))) {
                                    exactSourceLines++;
                                }
                            }
                        }
                    } catch (AssertionError failure) {
                        if (mismatches.length() < 10000) {
                            mismatches.append('\n').append(failure.getMessage());
                        }
                    } catch (IOException | RuntimeException failure) {
                        failedToDecompile++;
                        if (decompilationErrors.size() < 25) {
                            decompilationErrors.add(internalName + ": " + failure);
                        }
                    }
                    if (jarScanned % 100 == 0) {
                        System.out.println("Dependency line audit progress: " + marker
                                + " entry=" + jarOrdinal + ", scanned=" + jarScanned
                                + ", decompiled=" + jarDecompiled);
                    }
                }
            }
            System.out.println("Dependency line audit completed: " + marker
                    + " scanned=" + jarScanned + ", decompiled=" + jarDecompiled);
        }

        if (scanned == 0) {
            fail("No dependency classes were scanned");
        }

        System.out.println("Dependency line audit: scanned=" + scanned
                + ", decompiled=" + decompiled + ", decompilation errors=" + failedToDecompile
                + ", source lines compared=" + sourceLinesCompared
                + ", exact source lines=" + exactSourceLines
                + ", source lines out of range=" + sourceLinesOutOfRange
                + ", classes without source=" + classesWithoutSource
                + ", suppressed known line numbers=" + suppressedLineNumbers
                + ", blank statement lines matching debug source=" + blankLinesMatchingDebugSource);
        for (String error : decompilationErrors) {
            System.out.println("Dependency line audit decompilation error: " + error);
        }
        for (String example : suppressedExamples) {
            System.out.println("Dependency line audit suppressed known lines: " + example);
        }
        for (String example : blankDebugExamples) {
            System.out.println("Dependency line audit blank debug source line: " + example);
        }
        if (!mismatches.isEmpty()) {
            fail("Test dependency line number mismatches:" + mismatches);
        }
        if (sourceLinesOutOfRange > 0) {
            fail(sourceLinesOutOfRange + " numbered output lines exceed their source file length");
        }
    }

    private static String[] readSourceLines(JarFile sources, String sourceName) {
        JarEntry entry = sources.getJarEntry(sourceName);
        if (entry == null) {
            return null;
        }
        try (var input = sources.getInputStream(entry)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8).split("\\r\\n|\\n|\\r", -1);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read " + sourceName, exception);
        }
    }

    private static String normalizeLine(String line) {
        return line.replaceAll("\\s+", "");
    }

    private static Set<Integer> readDebugLines(JarFile jar, JarEntry entry) throws IOException {
        Set<Integer> lines = new HashSet<>();
        try (var input = jar.getInputStream(entry)) {
            for (org.apache.bcel.classfile.Method method : new ClassParser(input, entry.getName()).parse().getMethods()) {
                LineNumberTable table = method.getLineNumberTable();
                if (table != null) {
                    for (LineNumber line : table.getLineNumberTable()) {
                        lines.add(line.getLineNumber());
                    }
                }
            }
        }
        return lines;
    }

    private static boolean isStatementOrExpression(String text) {
        String trimmed = text.trim();
        return trimmed.endsWith(";") && !trimmed.startsWith("package ") && !trimmed.startsWith("import ");
    }
}
