# Line number audit — 2026-10-03

Reran **11,885 eligible class entries across the same 16 JARs** as the [previous audit](../line-number-audit-2026-10-02-after/README.md), using Temurin 17.0.20.1 and an immutable copy of production classes compiled with the Maven ECJ profile. Before means that previous working-tree audit, not a clean master checkout. [Manifest](manifest.json).

Known line placement improved from **97.25% to 98.29%**. Suppressed known numbers fell from **14,744 to 9,188**. Visible numeric mismatches: **0**.

| JAR | Version | Previous | Current | Suppressed known: previous → current | Errors | Bytecode fallback |
|---|---|---:|---:|---:|---:|---:|
| commons-lang3 | 3.20.0 | 98.16% | 98.16% | 290 → 290 | 0 | 0 |
| commons-io | 2.21.0 | 100.00% | 100.00% | 0 → 0 | 0 | 0 |
| commons-collections4 | 4.4 | 99.64% | 99.64% | 51 → 51 | 0 | 1 |
| commons-codec | 1.19.0 | 100.00% | 100.00% | 0 → 0 | 0 | 0 |
| commons-compress | 1.28.0 | 99.81% | 99.81% | 47 → 47 | 0 | 2 |
| jsoup | 1.16.2 | 100.00% | 100.00% | 0 → 0 | 0 | 7 |
| ecj | 3.44.0 | 94.07% | 96.47% | 6,786 → 4,044 | 0 | 36 |
| guava | 12.0 | 99.99% | 99.99% | 2 → 2 | 2 | 2 |
| gson | 2.13.2 | 99.98% | 99.98% | 1 → 1 | 0 | 1 |
| jackson-databind | 2.15.3 | 97.87% | 97.87% | 738 → 738 | 0 | 6 |
| log4j-core | 2.20.0 | 99.96% | 99.96% | 13 → 13 | 0 | 1 |
| slf4j-api | 2.0.17 | 100.00% | 100.00% | 0 → 0 | 0 | 0 |
| junit | 4.13.2 | 99.75% | 99.95% | 11 → 2 | 0 | 0 |
| asm | 9.9 | 100.00% | 100.00% | 0 → 0 | 0 | 0 |
| plantuml | 1.2023.12 | 98.72% | 99.46% | 1,495 → 625 | 0 | 0 |
| org.eclipse.jdt.core | 3.36.0 | 95.47% | 97.12% | 5,310 → 3,375 | 23 | 51 |

11,860 entries returned output; 25 threw errors. 107 outputs contain bytecode fallback and 1,632 outputs have no numeric comments. The error classes are unchanged from the previous working-tree audit; this comparison does not establish their status on master.

## Fixes and checks

- Bound complete individual instructions, including loop conditions, to the source range retained by the layouter. A loop-back-edge line must not expand its condition into the body and shift later statements.
- Give the inline record-component block a zero line span, matching the writer's actual output.
- Recognize generated record constructors of the record's own visibility, including private records. Keep constructors with broader explicit accessibility.
- Compare bytecode using erased descriptors, so generic record constructors and accessors are recognized correctly. Preserve generic component signatures in the printed record declaration.
- Add tests for loop bodies and following statements, private records with generic components, explicit public constructors and custom accessors. Recompile the generated record output with Java 17.
- Check nine repaired statements against original ECJ Scope and JDT JavaModelManager source files at exactly the same physical rows, including their numeric comments. These two outputs now have zero suppressed known numbers. [Source evidence](source-checks.json).

## Measurement

Known line placement = matched numeric comments / (all numeric comments + suppressed known numbers). Misplaced known numbers appear as blank comments; counting them as failures prevents a misleading 100% result. Numbers already unknown when they reach the printer remain blank and are excluded. This does not measure every missing expression assignment or prove whole-source text equivalence.

Every eligible class entry is decompiled with a fresh printer and decompiler. Nested entries can repeat source output, so counts represent occurrences. Exclude META-INF entries, module-info.class and package-info.class. The selected JAR takes precedence over classpath dependencies. CRLF, LF and CR alone delimit physical lines. Errors are excluded from line denominators and reported separately. Four scans run in parallel, in fresh JVM batches of at most 100 entries.

Remaining alignment gaps are recorded by class in [details.txt](details.txt).

The initial Commons Lang batch exceeded its 180-second limit. The isolated class completed successfully, and the missing batches completed with a 600-second limit and two workers. Completed batches were retained from the same immutable build.

## Verification and reproduction

Both full suites passed on Temurin 17.0.20.1: ECJ and a fresh Javac compilation each ran 312 tests with zero failures, zero errors and one skipped opt-in audit. Generated record output also compiled successfully with Javac 17. All nine targeted original-source row comparisons passed.

[Full suite counts](verification.json), [CSV](results.csv), [JSON](results.json), [checksums](manifest.json), [harness](JarAlignmentAudit.java) and [batch runner](audit.py).

Compile the project and set AUDIT_CP to its compiled classes plus the runtime dependencies in the manifest. Keep those classes fixed for the whole scan.

```sh
javac -cp "$AUDIT_CP" reports/line-number-audit-2026-10-03/JarAlignmentAudit.java
python3 reports/line-number-audit-2026-10-03/audit.py \
  --java "$JAVA_HOME/bin/java" \
  --classpath "reports/line-number-audit-2026-10-03:$AUDIT_CP" \
  --output /tmp/alignment-audit-oct03
```

The manifest uses local paths; adjust them on another machine while retaining artifact versions and checksums. Raw TSV columns: attempted, returned, errors, numeric comments, matched, mismatched, suppressed known numbers, bytecode fallback outputs, outputs without numeric comments.
