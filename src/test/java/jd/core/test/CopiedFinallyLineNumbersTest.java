package jd.core.test;

import jd.core.preferences.Preferences;
import jd.core.printer.PrinterImpl;
import jd.core.process.DecompilerImpl;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CopiedFinallyLineNumbersTest {
    @Test
    public void resourceCleanupDoesNotShiftFollowingMethods() throws Exception {
        Preferences preferences = new Preferences();
        preferences.setRealignmentLineNumber(true);
        preferences.setShowLineNumbers(true);
        preferences.setWriteMetaData(false);
        PrinterImpl printer = new PrinterImpl(preferences);
        String output = printer.buildDecompiledOutput(new ClassPathLoader(),
                "jd/core/test/CopiedFinallyLineNumbers", preferences, new DecompilerImpl());

        AbstractTestCase.assertRealignedLineNumbers("CopiedFinallyLineNumbers", output);
        assertTrue(output, output.contains("/* 32 */     return 42;"));
        assertEquals(output, 0, printer.getSuppressedLineNumberCount());
    }
}
