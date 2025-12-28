package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class RecordTest extends AbstractTestCase {
    @Test
    public void testRecordWithConstructor() throws Exception {
        String output = decompile("jd/core/test/RecordWithConstructor", "17");
        assertEquals(IOUtils.toString(getClass().getResource("RecordWithConstructor.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void testRecordWithOverrides() throws Exception {
        String output = decompile("jd/core/test/RecordWithOverrides", "17");
        assertEquals(IOUtils.toString(getClass().getResource("RecordWithOverrides.txt"), StandardCharsets.UTF_8), output);
    }
    
    @Test
    public void testRecordWithEmptyBody() throws Exception {
        String output = decompile("jd/core/test/RecordWithEmptyBody", "17");
        assertEquals(IOUtils.toString(getClass().getResource("RecordWithEmptyBody.txt"), StandardCharsets.UTF_8), output);
    }
    
    @Test
    public void testRecordWithGetters() throws Exception {
        String output = decompile("jd/core/test/RecordWithGetters", "17");
        assertEquals(IOUtils.toString(getClass().getResource("RecordWithGetters.txt"), StandardCharsets.UTF_8), output);
    }
    
    @Test
    public void testRecordWithGetters2() throws Exception {
        String output = decompile("jd/core/test/RecordWithGetters2", "17");
        assertEquals(IOUtils.toString(getClass().getResource("RecordWithGetters2.txt"), StandardCharsets.UTF_8), output);
    }
    
    @Override
    protected boolean realignmentLineNumber() {
        return true;
    }

    @Override
    protected boolean recompile() {
        return true;
    }
}
