package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class FileUtilsTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/io/FileUtils");
        assertEquals(IOUtils.toString(getClass().getResource("FileUtils.txt"), StandardCharsets.UTF_8), output);
    }
}
