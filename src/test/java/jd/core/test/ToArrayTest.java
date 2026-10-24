package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ToArrayTest extends AbstractTestCase {

    @Test
    public void test() throws IOException {
        // Collection.toArray(IntFunction) was added in Java 11.
        String output = decompile("jd/core/test/ToArray", "11");
        assertEquals(IOUtils.toString(getClass().getResource("ToArray.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void java8ValidationRejectsJava11Api() {
        AssertionError failure = assertThrows(AssertionError.class,
                () -> decompile("jd/core/test/ToArray", "1.8"));
        assertTrue(failure.getMessage(), failure.getMessage().contains("toArray"));
    }
}
