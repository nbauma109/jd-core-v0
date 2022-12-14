package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class IndexOfTest extends AbstractTestCase {

    @Test
    public void test() throws IOException {
        String output = decompile("jd/core/test/IndexOf");
        assertEquals(IOUtils.toString(getClass().getResource("IndexOf.txt"), StandardCharsets.UTF_8), output);
    }
}
