package jd.core.test;

import org.jd.core.v1.loader.ClassPathLoader;

import java.io.IOException;

import jd.core.preferences.Preferences;
import jd.core.printer.PlainTextPrinter;
import jd.core.process.DecompilerImpl;

public abstract class AbstractTestCase {

    protected String decompile(String internalTypeName) throws IOException {
        
        DecompilerImpl decompiler = new DecompilerImpl();
        
        ClassPathLoader loader = new ClassPathLoader();

        PlainTextPrinter printer = new PlainTextPrinter();

        Preferences preferences = new Preferences();
        preferences.setRealignmentLineNumber(true);
        preferences.setShowDefaultConstructor(true);
        preferences.setShowLineNumbers(true);
        preferences.setShowPrefixThis(true);
        preferences.setUnicodeEscape(false);
        preferences.setWriteMetaData(false);

        return printer.buildDecompiledOutput(loader, internalTypeName, preferences, decompiler);

    }
}
