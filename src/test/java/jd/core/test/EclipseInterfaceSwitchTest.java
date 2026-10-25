package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class EclipseInterfaceSwitchTest extends AbstractTestCase {
    @Test
    public void interfaceEnumSwitchHasNoSyntheticTableReferences() throws Exception {
        String internalClassName = "jd/core/test/syntheticcases/InterfaceSwitch";
        String output = decompile(internalClassName);
        assertEquals(IOUtils.toString(getClass().getResource("InterfaceSwitch.txt"), StandardCharsets.UTF_8), output);
    }
}
