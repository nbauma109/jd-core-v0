package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/*
 * FastInstructionListBuilder.isAForEachIteratorPattern() : 
 * fix for (??? = sorted.iterator(); ((Iterator)???).hasNext();) { String zoneName = (String)((Iterator)???).next();
 * 
 * ClassFileAnalyzer.analyseAndModifyConstructors() : Remove default constructors of inner classes 
 */
public class FastDateParserTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("org/apache/commons/lang3/time/FastDateParser");
        assertEquals(IOUtils.toString(getClass().getResource("FastDateParser.txt"), StandardCharsets.UTF_8), output);
    }
}
