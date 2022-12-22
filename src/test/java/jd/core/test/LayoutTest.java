package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class LayoutTest extends AbstractTestCase {
    @Test
    public void testExternalizable() throws Exception {
        String output = decompile("java/io/Externalizable");
        assertEquals(IOUtils.toString(getClass().getResource("Externalizable.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void testFruit() throws Exception {
        String output = decompile("jd/core/test/Fruit");
        assertEquals(IOUtils.toString(getClass().getResource("Fruit.txt"), StandardCharsets.UTF_8), output);
    }
}
