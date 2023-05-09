package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class FtpConnexionTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("net/sourceforge/plantuml/ftp/FtpConnexion");
        assertEquals(IOUtils.toString(getClass().getResource("FtpConnexion.txt"), StandardCharsets.UTF_8), output);
    }
}
