package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class HashCodeBuilderTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/lang3/builder/HashCodeBuilder");
        assertEquals(IOUtils.toString(getClass().getResource("HashCodeBuilder.txt"), StandardCharsets.UTF_8), output);
    }

    @Override
    public realignmentLineNumbers() {
        return false;
    }
}
