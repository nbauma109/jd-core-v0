package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class StreamsTest extends AbstractTestCase {

    @Test
    public void test() throws IOException {
    	try (InputStream in = getClass().getResourceAsStream("/commons-lang3-3.12.0.jar")) {
    		CompositeLoader loader = new CompositeLoader(in);
	        String output = decompile("org/apache/commons/lang3/Streams", loader);
	        assertEquals(IOUtils.toString(getClass().getResource("Streams.txt"), StandardCharsets.UTF_8), output);
    	}
    }

    @Test
    public void test2() throws IOException {
    	try (InputStream in = getClass().getResourceAsStream("/commons-lang3-3.12.0.jar")) {
    		CompositeLoader loader = new CompositeLoader(in);
	        String output = decompile("org/apache/commons/lang3/stream/Streams", loader);
	        assertEquals(IOUtils.toString(getClass().getResource("Streams2.txt"), StandardCharsets.UTF_8), output);
    	}
    }
}
