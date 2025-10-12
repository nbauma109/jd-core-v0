package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class StringUtilsTest extends AbstractTestCase {

    /*
     * Bug fix to recognise boolean array
     * LocalVariableAnalyzer.setConstantTypesArrayLoad(ConstantPool, LocalVariables, ArrayLoadInstruction)
     */
    @Test
    public void test() throws IOException {
    	try (InputStream in = getClass().getResourceAsStream("/commons-lang3-3.12.0.jar")) {
    		CompositeLoader loader = new CompositeLoader(in);
	        String output = decompile("org/apache/commons/lang3/StringUtils", loader);
	        assertEquals(IOUtils.toString(getClass().getResource("StringUtils.txt"), StandardCharsets.UTF_8), output);
    	}
    }
}
