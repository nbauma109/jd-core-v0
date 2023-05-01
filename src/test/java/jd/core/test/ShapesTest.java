package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ShapesTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("gen/lib/common/shapes__c");
        assertEquals(IOUtils.toString(getClass().getResource("shapes__c.txt"), StandardCharsets.UTF_8), output);
    }
}
