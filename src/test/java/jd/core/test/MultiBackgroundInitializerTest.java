package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class MultiBackgroundInitializerTest extends AbstractTestCase {

    @Test
    public void test() throws IOException {
        String output = decompile("org/apache/commons/lang3/concurrent/MultiBackgroundInitializer");
        assertEquals(IOUtils.toString(getClass().getResource("MultiBackgroundInitializer.txt"), StandardCharsets.UTF_8), output);
    }
}
