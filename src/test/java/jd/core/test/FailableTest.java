package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

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
        String output = decompile("org/apache/commons/lang3/function/Failable");
        assertEquals(IOUtils.toString(getClass().getResource("Failable.txt"), StandardCharsets.UTF_8), output);
    }
}
