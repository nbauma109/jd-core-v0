package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/** Compiled by javac or ecj, depending on the profile: the fixture is the one of the compiler. */
public class AssertionsInConditionsTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("jd/core/test/AssertionsInConditions");
        assertEquals(IOUtils.toString(expectedResource("AssertionsInConditions.txt"), StandardCharsets.UTF_8), output);
    }
}
