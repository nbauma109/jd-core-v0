package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/*
 * A test for compact store / return.
 */
public class LockingVisitorsTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/lang3/concurrent/locks/LockingVisitors");
        assertEquals(IOUtils.toString(getClass().getResource("LockingVisitors.txt"), StandardCharsets.UTF_8), output);
    }
}
