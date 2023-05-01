package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ClusterTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("gen/lib/dotgen/cluster__c");
        assertEquals(IOUtils.toString(getClass().getResource("cluster__c.txt"), StandardCharsets.UTF_8), output);
    }
}
