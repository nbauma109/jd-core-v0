package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/*
 * Test for exception aggregation.
 * FastCodeExceptionAnalyzer.aggregateCodeExceptions().
 */
public class EventUtilsTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/lang3/event/EventUtils");
        assertEquals(IOUtils.toString(getClass().getResource("EventUtils.txt"), StandardCharsets.UTF_8), output);
    }
}
