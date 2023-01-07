package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/*
 * Test for GenericExtendsSuperInterfacesLayoutBlock.
 */
public class PredicateDecoratorTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/collections4/functors/PredicateDecorator");
        assertEquals(IOUtils.toString(getClass().getResource("PredicateDecorator.txt"), StandardCharsets.UTF_8), output);
    }
}
