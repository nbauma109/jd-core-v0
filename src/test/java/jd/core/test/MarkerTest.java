package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class MarkerTest extends AbstractTestCase {

    @Test
    public void test() throws Exception {
        String output = decompile("ext/plantuml/com/ctreber/acearth/plugins/markers/Marker");
        assertEquals(IOUtils.toString(getClass().getResource("Marker.txt"), StandardCharsets.UTF_8), output);
    }
}
