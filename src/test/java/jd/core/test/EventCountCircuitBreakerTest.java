package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class EventCountCircuitBreakerTest extends AbstractTestCase {

    @Test
    public void test() throws IOException {
    	try (InputStream in = getClass().getResourceAsStream("/commons-lang3-3.12.0.jar")) {
    		ZipLoader loader = new ZipLoader(in);
	        String output = decompile("org/apache/commons/lang3/concurrent/EventCountCircuitBreaker", loader);
	        assertEquals(IOUtils.toString(getClass().getResource("EventCountCircuitBreaker.txt"), StandardCharsets.UTF_8), output);
	    }
    }
}
