package jd.core.test;

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