package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class BooleanUtilsTest extends AbstractTestCase {

    @Test
    public void test() throws IOException {
        String output = decompile("org/apache/commons/lang3/BooleanUtils");
        assertEquals(IOUtils.toString(getClass().getResource("BooleanUtils.txt"), StandardCharsets.UTF_8), output);
    }
}
