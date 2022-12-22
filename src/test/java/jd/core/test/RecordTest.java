package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class RecordTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("jd/core/model/classfile/accessor/GetFieldAccessor");
        assertEquals(IOUtils.toString(getClass().getResource("GetFieldAccessor.txt"), StandardCharsets.UTF_8), output);
    }

    @Override
    protected boolean realignmentLineNumber() {
        return false;
    }
}
