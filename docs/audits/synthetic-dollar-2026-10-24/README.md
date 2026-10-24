# Synthetic identifier audit

The manifest selects 361 binary jars already present in the local Maven repository,
across Apache, Google, Jackson/Woodstox, Eclipse, Spring, Netty, and logging libraries.
One version per artifact was selected deterministically by sorted repository path;
this is a diverse local corpus, not a claim to cover the latest release of each artifact.
Paths in `jars.txt` are relative to the Maven repository.

The scanner inventories class files, then decompiles a top-level class when it or
one of its nested classes contains a candidate synthetic name. Java tokenization
excludes matches in string literals and diagnostic bytecode comments. Candidates
still need manual inspection: Java identifiers may legitimately contain `$`.

Each jar runs in a separate JVM with a timeout. The summary records timeout and
process failures separately from successful scans. `BYTECODE_FALLBACK` records
methods the decompiler could not reconstruct; these do not count as fixed merely
because their diagnostic comments contain synthetic names.

## Results

| Measure | Result |
| --- | ---: |
| Jars inventoried | 361 |
| Class files (excluding multi-release entries and module/package metadata) | 65,923 |
| Top-level class decompilation attempts | 5,211 |
| Jars whose candidate scan completed | 348 |
| Jars whose scan hit the time limit (partial coverage) | 13 |
| Classes with identifier findings | 127, across 54 jars |
| Finding classes clear after fixes | 127 |
| Clear classes still containing unrelated bytecode fallback methods | 6 |

`inventory.tsv` records per-jar scope and scan status; `before-findings.tsv`
records token matches; `verification.tsv` records each focused rerun result.
`summary.json` contains the aggregate counts. Thirteen timeout jars were only
partially decompiled. This audit does not claim every class in the corpus can be
fully decompiled. Identifier removal in a class with bytecode fallback is reported
separately from complete source output.

The baseline is commit `b22b9a03a99de980998e59f4191593a52b109c8e`.
Audit worker JVMs used OpenJDK 25.0.2. Production and regression tests were also
validated on JDK 17 with both javac and ECJ.

## Reproduce

For comparable audit results, use JDK 25.0.2 and the jars listed in the manifest
(the project compiles its production and test classes for Java 17):

```sh
mvn -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/audit-classpath
python3 scripts/audit-synthetic-jars.py docs/audits/synthetic-dollar-2026-10-24/jars.txt \
  --output target/synthetic-audit --timeout 100
```

The output contains a JSON summary and a directory per jar with progress logs,
findings, and decompiled sources for candidate classes. `--repository` selects a
Maven repository outside `~/.m2/repository`. `--classes` and `--worker-classes`
allow comparing frozen builds without interfering with Maven compilation.

For a focused rerun, supply a jar manifest and `--targets` with tab-separated
jar paths and internal class names. Jars in the target file use the same path
convention as the manifest. The output directory must be new for each run.

## Confirmed patterns

- Anonymous constructor generic signatures can omit synthetic parameters. Use the
  JVM descriptor when locating the enclosing instance, and distinguish it from a
  captured reference, including one of the enclosing class's own type.
- Primitive static assignment accessors retain a duplicated return value. Resolve
  these `access$…` calls to assignments, with assignment precedence under casts.
- Captured fields may be referenced across multiple enclosing anonymous classes.
  Look up the field in the class that owns the reference.
- Older javac constructors can initialize unflagged captured fields after `super()`.
  Recover their constructor parameter mapping, including the first parameter in a
  static context, without treating ordinary user fields as captures.
- Enum constant bodies can use a switch map owned by an ancestor beyond the
  immediate outer class. Prepare synthetic maps before consumers and use the
  owning constant pool for enum case labels.

- Empty enum switches still evaluate the selector and reject null. Preserve the
  ordinal call instead of emitting a bare synthetic array access.
- Public ECJ interface switch-table helpers also need their case maps parsed.
- Constructor lambdas can capture the enclosing instance as a synthetic parameter;
  bind that parameter to the lexical enclosing instance. Lambdas capturing explicit
  constructor parameters must remain in the constructor rather than becoming field
  initializers with out-of-scope variable references.
- Newer compilers can omit unused enclosing-instance fields in inner subclasses.
  Their constructor parameter still represents the lexical enclosing instance.
- Assertions with boolean equality, assertions at the end of catch blocks, and
  assertions using an enclosing class's flag require additional reconstruction.
  Regression checks execute generated classes with assertions enabled and disabled.

Regression tests compile fixtures for Java 8 and Java 17, decompile them, compile
the generated source again, and execute both versions. A bytecode fixture also
recreates the old constructor ordering and missing synthetic field flags.
