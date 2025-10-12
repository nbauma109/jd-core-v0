package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

// ClassFileLayouter : Expand end block
public class DateUtilsTest extends AbstractTestCase {

    @Test
    public void test() throws IOException {
    	try (InputStream in = getClass().getResourceAsStream("/commons-lang3-3.12.0.jar")) {
    		CompositeLoader loader = new CompositeLoader(in);
	        String output = decompile("org/apache/commons/lang3/time/DateUtils", loader);
	        assertEquals(IOUtils.toString(getClass().getResource("DateUtils.txt"), StandardCharsets.UTF_8), output);
    	}
    }
}
