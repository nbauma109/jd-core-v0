package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class DeprecatedTest extends AbstractTestCase {

    @Test
    public void testDeprecatedMember() throws IOException {
        String output = decompile("jd/core/test/DeprecatedMember");
        assertEquals(IOUtils.toString(getClass().getResource("DeprecatedMember.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void testDeprecatedEmptyClass() throws IOException {
        String output = decompile("jd/core/test/DeprecatedEmptyClass");
        assertEquals(IOUtils.toString(getClass().getResource("DeprecatedEmptyClass.txt"), StandardCharsets.UTF_8), output);
    }
}
