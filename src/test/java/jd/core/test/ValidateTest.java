package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ValidateTest extends AbstractTestCase {

    @Test
    public void test() throws IOException {
        String output = decompile("org/apache/commons/lang3/Validate");
        assertEquals(IOUtils.toString(getClass().getResource("Validate.txt"), StandardCharsets.UTF_8), output);
    }

    @Override
    protected boolean recompile() {
        return false;
    }
}
