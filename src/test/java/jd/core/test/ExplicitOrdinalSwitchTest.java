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

    @Test
    public void preservesNumericCasesForNonEnumOrdinalMethod() throws IOException {
        String output = decompile("jd/core/test/NonEnumOrdinalSwitch");
        assertTrue(output.contains("switch (rank.ordinal())"));
        assertTrue(output.contains("case 0:"));
        assertTrue(output.contains("case 1:"));
    }

    @Test
    public void preservesNumericCasesForUnresolvableEnum() throws IOException {
        String output = decompile("jd/core/test/ExternalEnumOrdinalSwitch");
        assertTrue(output.contains("switch (day.ordinal())"));
        assertTrue(output.contains("case 0:"));
        assertTrue(output.contains("case 1:"));
    }
}
