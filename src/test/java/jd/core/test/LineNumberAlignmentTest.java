package jd.core.test;

import jd.core.preferences.Preferences;
import jd.core.printer.PrinterImpl;
import jd.core.process.DecompilerImpl;

import org.jd.core.v1.api.loader.Loader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LineNumberAlignmentTest extends AbstractTestCase {
    @Test
    public void keepsEveryLineNumberOnItsLine() throws Exception {
        String internalTypeName = "jd/core/test/LineNumberAlignment";
        Preferences preferences = new Preferences();
        preferences.setRealignmentLineNumber(true);
        preferences.setShowLineNumbers(true);
        PrinterImpl printer = new PrinterImpl(preferences);
        Loader loader = new org.jd.core.v1.loader.ClassPathLoader();

        String output = printer.buildDecompiledOutput(loader, internalTypeName, preferences, new DecompilerImpl());

        // The last method is the one which used to be shifted by the copies of
        // the "finally" blocks, the enum initializer and the lambda call.
        assertTrue(output, output.contains("return 42;"));
        assertEquals(output, 0, printer.getSuppressedLineNumberCount());
    }
}
