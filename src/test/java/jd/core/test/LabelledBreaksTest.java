package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/*
 * Test for labelled breaks.
 */
public class LabelledBreaksTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/labelled-breaks-jdk1.4.2_19.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/LabelledBreaks", loader);
            assertEquals(IOUtils.toString(getClass().getResource("LabelledBreaks.txt"), StandardCharsets.UTF_8), output);
        }
    }
    
}
