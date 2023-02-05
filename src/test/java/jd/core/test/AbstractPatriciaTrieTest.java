package jd.core.test;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractPatriciaTrieTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/collections4/trie/AbstractPatriciaTrie");
        assertEquals(-1, output.indexOf("access$"));
    }
}
