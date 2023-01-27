package jd.core.test;

import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;

import java.io.IOException;

import jd.core.preferences.Preferences;
import jd.core.printer.PlainTextPrinter;
import jd.core.process.DecompilerImpl;

public abstract class AbstractTestCase {

    protected String decompile(String internalTypeName, Loader loader) throws IOException {

        DecompilerImpl decompiler = new DecompilerImpl();

        PlainTextPrinter printer = new PlainTextPrinter();

        Preferences preferences = new Preferences();
        preferences.setRealignmentLineNumber(realignmentLineNumber());
        preferences.setShowDefaultConstructor(showDefaultConstructor());
        preferences.setShowLineNumbers(true);
        preferences.setShowPrefixThis(true);
        preferences.setUnicodeEscape(true);
        preferences.setWriteMetaData(showMetaData());

        return printer.buildDecompiledOutput(loader, internalTypeName, preferences, decompiler);

    }

    protected String decompile(String internalTypeName) throws IOException {
        return decompile(internalTypeName, new ClassPathLoader());
    }
    
    protected boolean showDefaultConstructor() {
        return false;
    }
    
    protected boolean showMetaData() {
        return false;
    }
    
    protected boolean realignmentLineNumber() {
        return true;
    }
}
