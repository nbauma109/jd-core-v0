package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class UnmodifiableTrieTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/collections4/trie/UnmodifiableTrie");
        assertEquals(IOUtils.toString(getClass().getResource("UnmodifiableTrie.txt"), StandardCharsets.UTF_8), output);
    }
}
