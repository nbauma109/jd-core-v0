package jd.core.test;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MacroPlacementTest {
    private static final String MACRO = "smetana/core/Macro";

    private static class Variant extends AbstractTestCase {
        private final boolean showLineNumbers;
        private final boolean realignment;

        Variant(boolean showLineNumbers, boolean realignment) {
            this.showLineNumbers = showLineNumbers;
            this.realignment = realignment;
        }

        @Override
        protected boolean showLineNumbers() {
            return showLineNumbers;
        }

        @Override
        protected boolean realignmentLineNumber() {
            return realignment;
        }
    }

    @Test
    public void testTheConstantsFollowTheMethodsEvenIfTheLineNumbersAreHidden() throws Exception {
        String output = new Variant(false, true).decompile(MACRO);

        assertTrue(output.indexOf("UNSURE_ABOUT") < output.indexOf("int AGRAPH"));
    }

    @Test
    public void testTheConstantsComeFirstWithoutRealignment() throws Exception {
        String output = new Variant(true, false).decompile(MACRO);

        assertTrue(output.indexOf("int AGRAPH") < output.indexOf("UNSURE_ABOUT"));
    }
}
