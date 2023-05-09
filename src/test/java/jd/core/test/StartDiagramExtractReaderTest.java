package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class StartDiagramExtractReaderTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("net/sourceforge/plantuml/preproc/StartDiagramExtractReader");
        assertEquals(IOUtils.toString(getClass().getResource("StartDiagramExtractReader.txt"), StandardCharsets.UTF_8), output);
    }
}
