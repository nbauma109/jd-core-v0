package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/*
 * Test for FastCodeExceptionAnalyzer.defineType(List<Instruction>, FastCodeExcepcion).
 */
public class BackgroundInitializerTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/lang3/concurrent/BackgroundInitializer");
        assertEquals(IOUtils.toString(getClass().getResource("BackgroundInitializer.txt"), StandardCharsets.UTF_8), output);
    }
}
