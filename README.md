[![](https://jitpack.io/v/nbauma109/jd-core-v0.svg)](https://jitpack.io/#nbauma109/jd-core-v0)
[![](https://jitci.com/gh/nbauma109/jd-core-v0/svg)](https://jitci.com/gh/nbauma109/jd-core-v0)
[![CodeQL](https://github.com/nbauma109/jd-core-v0/actions/workflows/codeql-analysis.yml/badge.svg?branch=master)](https://github.com/nbauma109/jd-core-v0/actions/workflows/codeql-analysis.yml)
[![Maven Release](https://github.com/nbauma109/jd-core-v0/actions/workflows/maven.yml/badge.svg)](https://github.com/nbauma109/jd-core-v0/actions/workflows/maven.yml)
[![Github Release](https://github.com/nbauma109/jd-core-v0/actions/workflows/release.yml/badge.svg)](https://github.com/nbauma109/jd-core-v0/actions/workflows/release.yml)

# JD-Core v0

Original version of jd-core based on byte code pattern matching

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

            String out = printer.buildDecompiledOutput(loader, "java/lang/String", preferences, DECOMPILER);

            System.out.println(out);

        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
```
