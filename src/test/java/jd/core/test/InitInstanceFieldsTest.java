package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class InitInstanceFieldsTest extends AbstractTestCase {

    @Test
    public void test() throws IOException {
        String output = decompile("jd/core/test/InitInstanceFields");
        assertEquals(IOUtils.toString(getClass().getResource("InitInstanceFields.txt"), StandardCharsets.UTF_8), output);
    }
}
