package jd.core.test;

import org.junit.Test;

public class LineNumberAuditTest {
    @Test
    public void unicodeSeparatorsInsideCharacterConstantsAreNotPhysicalLines() {
        String output = "/* 1 */ char separator = '" + (char) 0x2028 + "';\n"
                + "/* 2 */ char paragraph = '" + (char) 0x2029 + "';\n"
                + "/* 3 */ int value = 1;\n";

        AbstractTestCase.assertRealignedLineNumbers("UnicodeSeparators", output);
    }
}
