package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class UGraphicTxtTest extends AbstractTestCase {

    @Test
    public void test() throws Exception {
        String output = decompile("net/sourceforge/plantuml/klimt/drawing/txt/UGraphicTxt");
        assertEquals(IOUtils.toString(getClass().getResource("UGraphicTxt.txt"), StandardCharsets.UTF_8), output);
    }
}
