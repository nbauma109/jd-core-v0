package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class SwitchEnumTest extends AbstractTestCase {
    @Test
    public void testSwitchEnum() throws Exception {
        String internalClassName = "jd/core/test/SwitchEnum";
        String output = decompile(internalClassName);
        assertEquals(IOUtils.toString(getClass().getResource("SwitchEnum.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void testEnumSwitch() throws Exception {
        String internalClassName = "jd/core/test/EnumSwitch";
        String output = decompile(internalClassName);
        assertEquals(IOUtils.toString(getClass().getResource("EnumSwitch.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void testSwitchEnumJDK180() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/switch-enum-jdk1.8.0_331.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/SwitchEnum";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("SwitchEnum.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Test
    public void testSwitchEnumInnerClassJDK180() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/switch-enum-inner-class-jdk1.8.0_331.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/SwitchEnumInnerClass";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("SwitchEnumInnerClass.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
