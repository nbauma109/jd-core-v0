package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ForEachTest extends AbstractTestCase {

    @Test
    public void test() throws IOException {
        String output = decompile("jd/core/test/ForEach");
        assertEquals(IOUtils.toString(getClass().getResource("ForEach.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void testFileAlterationObserver() throws IOException {
        String output = decompile("org/apache/commons/io/monitor/FileAlterationObserver");
        assertEquals(IOUtils.toString(getClass().getResource("FileAlterationObserver.txt"), StandardCharsets.UTF_8), output);
    }
}
