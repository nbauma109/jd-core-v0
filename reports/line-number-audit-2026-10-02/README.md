# Line number audit — 2026-10-02

Audited the current working tree with realignment and line number comments enabled, using Temurin 17.0.20.1 (the Java 17 distribution configured in Maven CI). Production compilation succeeded before the audit. The source snapshot and JAR checksums are recorded in [manifest.json](manifest.json).

## Method

- Exhaustively visit every eligible class entry in each selected JAR, including nested and anonymous classes. Exclude `META-INF/` entries (including multi-release variants), `module-info.class`, and `package-info.class`.
- Load the selected JAR before classpath dependencies, avoiding accidental substitution by a different dependency version.
- Decompile each entry with a fresh printer and compare each leading numeric source comment against the physical output line, counted from 1. Only CRLF, LF and CR are line separators; Unicode character literals do not create physical lines.
- Count known source numbers suppressed by `PrinterImpl` because their requested number differs from the physical line. These appear as blank comments. Numbers already unknown when they reach the printer remain blank and are excluded from the percentage; missing assignments earlier in the pipeline are not measured by this audit.
- **Visible comment agreement** = matching numeric comments / all numeric comments.
- **Known line placement** = matching numeric comments / (all numeric comments + suppressed known numbers). This is the useful realignment percentage: the current printer hides misplaced known numbers, so visible agreement alone overstates alignment.
- Counts describe output line occurrences. Separately decompiling nested classes can repeat source lines, and a line containing several expressions contributes one printed line number. This does not measure expression coverage or prove the decompiled text matches original source text.
- A thrown exception excludes that class from line denominators; it remains an error in the class totals. Returned output with bytecode fallback is counted and flagged separately.
- Run batches of at most 100 entries in fresh JVMs, with two independent JAR scans in parallel.

## Results

11,885/11,885 eligible entries attempted across 16 JARs; 11,860 returned output and 25 threw errors. 107 returned outputs contain bytecode fallback; 1,636 returned outputs have no numeric comments.

Across returned outputs: **0 visible numeric mismatches**, **51,404 suppressed known numbers**, **90.38% known line placement**.

| JAR | Version | Returned / eligible | Errors | Bytecode fallback | Matching numeric | Numeric mismatches | Suppressed known | Visible agreement | Known line placement |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|
| commons-lang3 | 3.20.0 | 403 / 403 | 0 | 0 | 15,139 | 0 | 650 | 100.00% | 95.88% |
| commons-io | 2.21.0 | 383 / 383 | 0 | 0 | 8,233 | 0 | 52 | 100.00% | 99.37% |
| commons-collections4 | 4.4 | 524 / 524 | 0 | 1 | 14,031 | 0 | 82 | 100.00% | 99.42% |
| commons-codec | 1.19.0 | 108 / 108 | 0 | 0 | 4,550 | 0 | 14 | 100.00% | 99.69% |
| commons-compress | 1.28.0 | 553 / 553 | 0 | 2 | 22,728 | 0 | 1,330 | 100.00% | 94.47% |
| jsoup | 1.16.2 | 258 / 258 | 0 | 7 | 10,380 | 0 | 438 | 100.00% | 95.95% |
| ecj | 3.44.0 | 807 / 807 | 0 | 36 | 88,027 | 0 | 26,399 | 100.00% | 76.93% |
| guava | 12.0 | 1,330 / 1,332 | 2 | 2 | 29,766 | 0 | 14 | 100.00% | 99.95% |
| gson | 2.13.2 | 198 / 198 | 0 | 1 | 5,188 | 0 | 6 | 100.00% | 99.88% |
| jackson-databind | 2.15.3 | 756 / 756 | 0 | 6 | 33,643 | 0 | 947 | 100.00% | 97.26% |
| log4j-core | 2.20.0 | 1,164 / 1,164 | 0 | 1 | 29,052 | 0 | 73 | 100.00% | 99.75% |
| slf4j-api | 2.0.17 | 55 / 55 | 0 | 0 | 1,179 | 0 | 0 | 100.00% | 100.00% |
| junit | 4.13.2 | 350 / 350 | 0 | 0 | 4,339 | 0 | 11 | 100.00% | 99.75% |
| asm | 9.9 | 38 / 38 | 0 | 0 | 3,781 | 0 | 1,352 | 100.00% | 73.66% |
| plantuml | 1.2023.12 | 3,543 / 3,543 | 0 | 0 | 114,491 | 0 | 1,547 | 100.00% | 98.67% |
| org.eclipse.jdt.core | 3.36.0 | 1,390 / 1,413 | 23 | 51 | 98,597 | 0 | 18,489 | 100.00% | 84.21% |

## Artifacts and reproduction

- [CSV counts](results.csv), [complete JSON results](results.json), and [class names with errors, bytecode fallbacks or numeric mismatches](details.txt).
- [Standalone audit harness](JarAlignmentAudit.java), taking four arguments: JAR path, start ordinal, maximum class count, and output TSV path.
- Compile the harness with Java 17 and a classpath containing this project's freshly compiled `target/classes` and runtime dependencies. Run it with the same classpath plus the harness class directory. The manifest lists the exact JAR paths used here.
- Harness TSV columns: attempted, returned, errors, numeric comments, matched, mismatched, suppressed known numbers, bytecode fallback classes, classes without numeric comments. Subsequent lines identify errors/fallbacks/mismatches.

```sh
javac -cp "$AUDIT_CP" reports/line-number-audit-2026-10-02/JarAlignmentAudit.java
java -Xmx768m -cp "reports/line-number-audit-2026-10-02:$AUDIT_CP" JarAlignmentAudit "$JAR" 0 100 /tmp/alignment-batch.tsv
```

Set `AUDIT_CP` to the project's compiled classes and runtime dependency JARs, and `JAR` to the selected manifest path. Increment the start ordinal by 100 until all eligible entries are visited. Unknown line numbers remain empty; auditing does not alter production output.
