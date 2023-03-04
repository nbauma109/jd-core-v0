package jd.core.test;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class ThisDotValDollarInLambdaTest extends AbstractTestCase {

    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/lang3/ClassUtils");
        assertNotNull(output);
        assertEquals(-1, output.indexOf("this.val$"));
    }

    @Test
    public void testFalsePositive() throws Exception {
        String output = decompile("jd/core/test/ThisDotValDollar");
        assertNotNull(output);
        assertNotEquals(-1, output.indexOf("this.val$"));
    }
}
