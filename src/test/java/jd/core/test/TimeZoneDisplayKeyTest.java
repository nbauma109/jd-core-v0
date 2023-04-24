package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/*
 * Test for bug fix: malformed empty if statement.
 */
public class TimeZoneDisplayKeyTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/lang3/time/FastDatePrinter$TimeZoneDisplayKey");
        assertEquals(IOUtils.toString(getClass().getResource("TimeZoneDisplayKey.txt"), StandardCharsets.UTF_8), output);
    }

    @Override
    protected boolean recompile() {
        return false;
    }
}
