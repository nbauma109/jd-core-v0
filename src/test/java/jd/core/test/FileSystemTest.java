package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class FileSystemTest extends AbstractTestCase {

    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/io/FileSystem");
        assertEquals(IOUtils.toString(getClass().getResource("FileSystem.txt"), StandardCharsets.UTF_8), output);
    }
}
