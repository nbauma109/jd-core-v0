package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ArrayReferenceTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/eclipse/jdt/internal/compiler/ast/ArrayReference");
        assertEquals(IOUtils.toString(getClass().getResource("ArrayReference.txt"), StandardCharsets.UTF_8), output);
    }
}
