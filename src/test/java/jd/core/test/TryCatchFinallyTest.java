package jd.core.test;

import org.junit.Test;

public class TryCatchFinallyTest extends AbstractTestCase {
    @Test
    public void testTryCatchFinallyTest() throws Exception {
        String internalClassName = "jd/core/test/TryCatchFinally";
        String source = decompile(internalClassName);
        System.out.println(source);
    }
}
