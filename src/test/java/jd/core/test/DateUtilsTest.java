package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

// ClassFileLayouter : Expand end block
public class DateUtilsTest extends AbstractTestCase {

    @Test
    public void test() throws IOException {
        String output = decompile("org/apache/commons/lang3/time/DateUtils");
        assertEquals(IOUtils.toString(getClass().getResource("DateUtils.txt"), StandardCharsets.UTF_8), output);
    }
}
