package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TypeArgumentTest extends AbstractTestCase {
    @Test
    public void testTypeArgument() throws Exception {
        String internalClassName = "jd/core/test/TypeArgument";
        String output = decompile(internalClassName);
        assertEquals(IOUtils.toString(getClass().getResource("TypeArgument.txt"), StandardCharsets.UTF_8), output);
    }
}
