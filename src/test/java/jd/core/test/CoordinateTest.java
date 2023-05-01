package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class CoordinateTest extends AbstractTestCase {

    @Test
    public void test() throws Exception {
        String output = decompile("ext/plantuml/com/ctreber/acearth/util/Coordinate");
        assertEquals(IOUtils.toString(getClass().getResource("Coordinate.txt"), StandardCharsets.UTF_8), output);
    }
}
