package jd.core.test;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EclipseInterfaceSwitchTest extends AbstractTestCase {
    @Test
    public void interfaceEnumSwitchHasNoSyntheticTableReferences() throws Exception {
        String source = decompile("jd/core/test/syntheticcases/InterfaceSwitch");
        assertFalse(source, source.contains("$SWITCH_TABLE$"));
        assertFalse(source, source.contains("$SwitchMap$"));
        assertTrue(source, source.contains("case FIRST:"));
        assertTrue(source, source.contains("case SECOND:"));
    }
}
