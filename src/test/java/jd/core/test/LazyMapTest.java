package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/*
 * Test for AddCheckCastVisitor.
 */
public class LazyMapTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/collections4/map/LazyMap");
        assertEquals(IOUtils.toString(getClass().getResource("LazyMap.txt"), StandardCharsets.UTF_8), output);
    }
}
