package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
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
    	try (InputStream in = getClass().getResourceAsStream("/commons-lang3-3.12.0.jar")) {
    		CompositeLoader loader = new CompositeLoader(in);
	        String output = decompile("org/apache/commons/lang3/time/FastDateParser", loader);
	        assertEquals(IOUtils.toString(getClass().getResource("FastDateParser.txt"), StandardCharsets.UTF_8), output);
    	}
    }
}
