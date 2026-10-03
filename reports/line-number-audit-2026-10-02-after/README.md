# Line number audit after fixes — 2026-10-02

Reran all **11,885 eligible class entries in the same 16 JARs** used by the [before report](../line-number-audit-2026-10-02/README.md). Temurin **17.0.20.1** was used, matching the Java 17 distribution configured in Maven CI. The audit used an immutable copy of freshly compiled production classes. [Manifest and checksums](manifest.json).

The before column refers to the earlier working-tree audit, not a clean `master` checkout.

## Results

Known line placement improved from **90.38% to 97.25%**. Suppressed known numbers fell from **51,404 to 14,744**. Visible numeric mismatches: **0**.

| JAR | Version | Before | After | Suppressed known: before → after | Errors | Bytecode fallback |
|---|---|---:|---:|---:|---:|---:|
| commons-lang3 | 3.20.0 | 95.88% | 98.16% | 650 → 290 | 0 | 0 |
| commons-io | 2.21.0 | 99.37% | 100.00% | 52 → 0 | 0 | 0 |
| commons-collections4 | 4.4 | 99.42% | 99.64% | 82 → 51 | 0 | 1 |
| commons-codec | 1.19.0 | 99.69% | 100.00% | 14 → 0 | 0 | 0 |
| commons-compress | 1.28.0 | 94.47% | 99.81% | 1,330 → 47 | 0 | 2 |
| jsoup | 1.16.2 | 95.95% | 100.00% | 438 → 0 | 0 | 7 |
| ecj | 3.44.0 | 76.93% | 94.07% | 26,399 → 6,786 | 0 | 36 |
| guava | 12.0 | 99.95% | 99.99% | 14 → 2 | 2 | 2 |
| gson | 2.13.2 | 99.88% | 99.98% | 6 → 1 | 0 | 1 |
| jackson-databind | 2.15.3 | 97.26% | 97.87% | 947 → 738 | 0 | 6 |
| log4j-core | 2.20.0 | 99.75% | 99.96% | 73 → 13 | 0 | 1 |
| slf4j-api | 2.0.17 | 100.00% | 100.00% | 0 → 0 | 0 | 0 |
| junit | 4.13.2 | 99.75% | 99.75% | 11 → 11 | 0 | 0 |
| asm | 9.9 | 73.66% | 100.00% | 1,352 → 0 | 0 | 0 |
| plantuml | 1.2023.12 | 98.67% | 98.72% | 1,547 → 1,495 | 0 | 0 |
| org.eclipse.jdt.core | 3.36.0 | 84.21% | 95.47% | 18,489 → 5,310 | 23 | 51 |

11,860 entries returned output; 25 threw errors. 107 returned outputs contain bytecode fallback; 1,634 returned outputs contain no numeric comments. Errors remain excluded from line denominators and are recorded separately.

## What changed

- Bound copied cleanup instructions to their source range, avoiding a one-line spill into later code.
- Separate lambda arguments from the preceding expression's source range, and handle initializers consisting entirely of a lambda.
- Start casts, receivers, declarations and assignments on their first known expression line, preserving multiline call chains.
- Keep oversized bytecode fallback comments inside the failed method's source span when later numbered code follows. All disassembly records remain present; records may share a comment line.
- Add tests for copied cleanup, multiline lambda arguments and assigned call chains, field lambdas, missing receiver numbers, and fallback disassembly before a numbered method.

## Targeted original-source checks

Checked 20 affected lines in ECJ `ClassFile`, Commons IO `FileUtils` and Commons Codec `Rule` against their matching source JARs. Require the same selected snippet at the exact original source row and a numeric output comment matching that physical row. All 20 passed, and these three outputs have zero suppressed known numbers. [Detailed evidence and source checksums](source-checks.json). These checks cover the repaired cases; they are not whole-source equivalence checks.

## Measurement and limits

- Realignment and numeric comments are enabled. Each leading numeric comment is compared with its physical output line, counted from 1. CRLF, LF and CR are the only physical line separators.
- **Known line placement = matched numeric comments / (all numeric comments + suppressed known numbers).** The printer hides misplaced known numbers as blank comments. Counting those blanks as failures avoids a misleading 100% visible-agreement result.
- Numbers already unknown when they reach the printer remain empty and are excluded. This report does not measure all missing expression assignments earlier in the pipeline.
- Every eligible class entry is visited, including nested classes. Exclude `META-INF/`, `module-info.class` and `package-info.class`. Independently decompiling nested classes can repeat source lines; counts represent printed occurrences.
- The selected JAR is loaded ahead of classpath dependencies. Four independent scans run in parallel, using fresh JVMs for batches of at most 100 entries and a fresh printer/decompiler per entry.
- This compares source numbers with physical output lines. It does not prove text equivalence with the original sources. Remaining suppressed known numbers are alignment gaps, detailed by class in [details.txt](details.txt).

## Verification

Both full suites passed on Temurin 17.0.20.1: `mvn -o -q test` and a fresh ECJ compilation followed by `mvn -o -q -Pecj test`. Each suite reports 310 tests, zero failures, zero errors and one opt-in dependency audit skipped. The standalone exhaustive JAR scan above was executed separately. [Verification counts](verification.json).

## Reproduce

[CSV counts](results.csv), [complete JSON results](results.json), [class-level details](details.txt), [audit harness](JarAlignmentAudit.java), [batch runner](audit.py), and [test verification](verification.json).

Compile the project first. Set `AUDIT_CP` to freshly compiled `target/classes` plus the runtime dependency JARs listed in the manifest. Use Java 17 for the harness and runner:

```sh
javac -cp "$AUDIT_CP" reports/line-number-audit-2026-10-02-after/JarAlignmentAudit.java
python3 reports/line-number-audit-2026-10-02-after/audit.py \
  --java "$JAVA_HOME/bin/java" \
  --classpath "reports/line-number-audit-2026-10-02-after:$AUDIT_CP" \
  --output /tmp/alignment-audit-after
```

The manifest contains local JAR paths; update paths if reproducing on another machine, preserving artifact versions and checksums. Keep compiled classes fixed during a scan. The runner's `summary.json` and batch TSVs contain the raw counts. TSV columns: attempted, returned, errors, numeric comments, matched, mismatched, suppressed known numbers, bytecode fallback classes, classes without numeric comments.
