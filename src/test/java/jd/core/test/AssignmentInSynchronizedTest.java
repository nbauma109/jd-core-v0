package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class AssignmentInSynchronizedTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("jd/core/test/AssignmentInSynchronized");
        assertEquals(IOUtils.toString(getClass().getResource("AssignmentInSynchronized.txt"), StandardCharsets.UTF_8), output);
    }
}
