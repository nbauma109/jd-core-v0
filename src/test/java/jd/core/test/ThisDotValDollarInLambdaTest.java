package jd.core.test;

import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class ThisDotValDollarInLambdaTest extends AbstractTestCase {

    /*
     * SourceWriterVisitor.writeGetField(GetField)
     */
    @Test
    public void test() throws Exception {
    	try (InputStream in = getClass().getResourceAsStream("/commons-lang3-3.12.0.jar")) {
    		CompositeLoader loader = new CompositeLoader(in);
	        String output = decompile("org/apache/commons/lang3/ClassUtils", loader);
	        assertNotNull(output);
	        assertEquals(-1, output.indexOf("this.val$"));
    	}
    }

    @Test
    public void testFalsePositive() throws Exception {
        String output = decompile("jd/core/test/ThisDotValDollar");
        assertNotNull(output);
        assertNotEquals(-1, output.indexOf("this.val$"));
    }
}
