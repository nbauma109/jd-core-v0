[![Dependabot updates](https://img.shields.io/badge/Dependabot-Updates-025E8C?logo=dependabot&logoColor=white)](https://github.com/nbauma109/jd-core-v0/network/updates)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.nbauma109/jd-core-v0.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.nbauma109/jd-core-v0)
[![CodeQL](https://github.com/nbauma109/jd-core-v0/actions/workflows/codeql-analysis.yml/badge.svg?branch=master)](https://github.com/nbauma109/jd-core-v0/actions/workflows/codeql-analysis.yml)
[![Maven Release](https://github.com/nbauma109/jd-core-v0/actions/workflows/maven.yml/badge.svg)](https://github.com/nbauma109/jd-core-v0/actions/workflows/maven.yml)
[![Github Release](https://github.com/nbauma109/jd-core-v0/actions/workflows/release.yml/badge.svg)](https://github.com/nbauma109/jd-core-v0/actions/workflows/release.yml)
[![Coverage Status](https://codecov.io/gh/nbauma109/jd-core-v0/branch/master/graph/badge.svg)](https://app.codecov.io/gh/nbauma109/jd-core-v0)

# JD-Core v0

A Java decompiler built on top of Emmanuel Dupuy's original version of [jd-core](https://github.com/java-decompiler/jd-core/tree/branch-jd-core-v0) based on bytecode pattern matching

## Code sample

```java
import org.jd.core.v1.loader.ClassPathLoader; // uses v1 loader for compatibility

import jd.core.preferences.Preferences;
import jd.core.printer.PlainTextPrinter;
import jd.core.process.DecompilerImpl;

public class Sample {

    private static final DecompilerImpl DECOMPILER = new DecompilerImpl();

    public static void main(String[] args) {
        try {
            ClassPathLoader loader = new ClassPathLoader();

            PlainTextPrinter printer = new PlainTextPrinter();

            Preferences preferences = new Preferences();
            preferences.setRealignmentLineNumber(true);
            preferences.setShowDefaultConstructor(true);
            preferences.setShowLineNumbers(true);
            preferences.setShowPrefixThis(true);
            preferences.setUnicodeEscape(false);
            preferences.setWriteMetaData(true);

            String out =
                    printer.buildDecompiledOutput(
                            loader,
                            "java/lang/String",
                            preferences,
                            DECOMPILER
                    );

            System.out.println(out);

        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
```

## Line number audit

Run tests with JDK 17, as in the main CI workflow. Line numbers are always displayed when the option is on; the dedicated alignment tests check that numeric line comments match their physical output line where realignment is possible. To scan every class in the direct test dependency JARs and compare numbered lines with the matching source JARs, first fetch the test sources, then run:

```sh
mvn dependency:sources -DincludeScope=test
mvn -Dtest=DependencyJarLineNumberAuditTest -DlineAudit.dependencies=true test
```

For large JARs, use `-DlineAudit.marker=net/sourceforge/plantuml/SourceStringReader.class` with `-DlineAudit.start=0 -DlineAudit.limit=1000`, then advance `lineAudit.start` by 1000 for each batch. The audit reports classes that could not be decompiled separately from line number mismatches. Exact source text matches are reported as evidence; reconstructed Java can differ from the original text while keeping the correct line number.

To audit another local JAR and its adjacent `-sources.jar`, set `-DlineAudit.jar=/path/to/library.jar`; `-DlineAudit.start` and `-DlineAudit.limit` also work for this mode. The summary reports misaligned line numbers: source numbers that are displayed but could not be placed on the corresponding physical output line.
