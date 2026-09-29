package jd.core.test;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class ExplicitOrdinalSwitchTest extends AbstractTestCase {
    @Test
    public void preservesNumericCasesOutsideEnumRange() throws IOException {
        String output = decompile("jd/core/test/ExplicitOrdinalSwitch");
        assertTrue(output.contains("switch (colour.ordinal())"));
        assertTrue(output.contains("case -1:"));
        assertTrue(output.contains("case 100:"));
    }
}
