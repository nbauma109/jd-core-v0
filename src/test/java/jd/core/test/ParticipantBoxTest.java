package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ParticipantBoxTest extends AbstractTestCase {

    @Test
    public void test() throws Exception {
        String output = decompile("net/sourceforge/plantuml/sequencediagram/graphic/ParticipantBox");
        assertEquals(IOUtils.toString(getClass().getResource("ParticipantBox.txt"), StandardCharsets.UTF_8), output);
    }
}
