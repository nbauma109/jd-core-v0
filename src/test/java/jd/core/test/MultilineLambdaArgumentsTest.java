package jd.core.test;

import jd.core.preferences.Preferences;
import jd.core.printer.PrinterImpl;
import jd.core.process.DecompilerImpl;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MultilineLambdaArgumentsTest {
    @Test
    public void keepsEachLambdaArgumentAndFollowingStatementOnItsSourceLine() throws Exception {
        Preferences preferences = new Preferences();
        preferences.setRealignmentLineNumber(true);
        preferences.setShowLineNumbers(true);
        preferences.setWriteMetaData(false);
        PrinterImpl printer = new PrinterImpl(preferences);
        String output = printer.buildDecompiledOutput(new ClassPathLoader(),
                "jd/core/test/MultilineLambdaArguments", preferences, new DecompilerImpl());

        AbstractTestCase.assertRealignedLineNumbers("MultilineLambdaArguments", output);
        String[] lines = output.split("\\r\\n|\\n|\\r", -1);
        assertTrue(output, lines[12].startsWith("/* 13 */") && lines[12].contains(" -> "));
        assertTrue(output, lines[13].startsWith("/* 14 */") && lines[13].contains("() -> "));
        assertTrue(output, lines[14].startsWith("/* 15 */") && lines[14].contains("return result;"));
        assertTrue(output, lines[23].startsWith("/* 24 */") && lines[23].contains(".map("));
        assertTrue(output, lines[24].startsWith("/* 25 */") && lines[24].contains(".reduce("));
        assertTrue(output, lines[25].startsWith("/* 26 */") && lines[25].contains(".orElse("));
        assertTrue(output, lines[29].startsWith("/* 30 */") && lines[29].contains("result = "));
        assertTrue(output, lines[30].startsWith("/* 31 */") && lines[30].contains(".map("));
        assertTrue(output, lines[31].startsWith("/* 32 */") && lines[31].contains(".reduce("));
        assertTrue(output, lines[32].startsWith("/* 33 */") && lines[32].contains(".orElse("));
        assertTrue(output, lines[33].startsWith("/* 34 */") && lines[33].contains("return result"));
        assertTrue(output, lines[37].startsWith("/* 38 */") && lines[37].contains("firstLength"));
        assertTrue(output, lines[38].startsWith("/* 39 */") && lines[38].contains("secondLength"));
        assertTrue(output, lines[39].startsWith("/* 40 */") && lines[39].contains("return firstLength"));
        assertEquals(output, 0, printer.getMisalignedLineNumberCount());
    }
}
