package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class StringUtilsTest extends AbstractTestCase {

    /*
     * Bug fix to recognise boolean array
     * LocalVariableAnalyzer.setConstantTypesArrayLoad(ConstantPool, LocalVariables, ArrayLoadInstruction)
     */
    @Test
    public void test() throws IOException {
        String output = decompile("org/apache/commons/lang3/StringUtils");
        assertEquals(IOUtils.toString(getClass().getResource("StringUtils.txt"), StandardCharsets.UTF_8), output);
    }
}
