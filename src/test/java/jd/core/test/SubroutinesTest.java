package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class SubroutinesTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/bcel/verifier/structurals/Subroutines");
        assertEquals(IOUtils.toString(getClass().getResource("Subroutines.txt"), StandardCharsets.UTF_8), output);
    }
}
