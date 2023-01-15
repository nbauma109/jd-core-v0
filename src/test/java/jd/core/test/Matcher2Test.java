package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class Matcher2Test extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("net/sourceforge/plantuml/command/regex/Matcher2");
        assertEquals(IOUtils.toString(getClass().getResource("Matcher2.txt"), StandardCharsets.UTF_8), output);
    }
}
