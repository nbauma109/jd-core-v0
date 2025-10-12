package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class FailableTest extends AbstractTestCase {

    /*
     * Tests a corner case of foreach array pattern.
     * FastInstructionListBuilder.createForEachVariableInstruction(Instruction, LocalVariables)
     *     case Const.ASTORE
     */
    @Test
    public void test() throws Exception {
    	try (InputStream in = getClass().getResourceAsStream("/commons-lang3-3.12.0.jar")) {
    		ZipLoader loader = new ZipLoader(in);
	        String output = decompile("org/apache/commons/lang3/function/Failable", loader);
	        assertEquals(IOUtils.toString(getClass().getResource("Failable.txt"), StandardCharsets.UTF_8), output);
    	}
    }
}
