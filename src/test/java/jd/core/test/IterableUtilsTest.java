package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class IterableUtilsTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/collections4/IterableUtils");
        assertEquals(IOUtils.toString(getClass().getResource("IterableUtils.txt"), StandardCharsets.UTF_8), output);
    }

    @Override
    protected boolean recompile() {
        return false;
    }
}
