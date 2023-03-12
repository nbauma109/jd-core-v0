package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class StreamsTest extends AbstractTestCase {

    @Test
    public void test() throws IOException {
        String output = decompile("org/apache/commons/lang3/Streams");
        assertEquals(IOUtils.toString(getClass().getResource("Streams.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void test2() throws IOException {
        String output = decompile("org/apache/commons/lang3/stream/Streams");
        assertEquals(IOUtils.toString(getClass().getResource("Streams2.txt"), StandardCharsets.UTF_8), output);
    }
}
