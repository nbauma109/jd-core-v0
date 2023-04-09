package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ThreadLocalWithInitialTest extends AbstractTestCase {

    @Test
    public void testAnonymous() throws IOException {
        String output = decompile("jd/core/test/ThreadLocalWithInitialAnonymous");
        assertEquals(IOUtils.toString(getClass().getResource("ThreadLocalWithInitialAnonymous.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void testLambda() throws IOException {
        String output = decompile("jd/core/test/ThreadLocalWithInitialLambda");
        assertEquals(IOUtils.toString(getClass().getResource("ThreadLocalWithInitialLambda.txt"), StandardCharsets.UTF_8), output);
    }
}
