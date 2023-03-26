package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class PathVisitorFileFilterTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/io/filefilter/PathVisitorFileFilter");
        assertEquals(IOUtils.toString(getClass().getResource("PathVisitorFileFilter.txt"), StandardCharsets.UTF_8), output);
    }
}
