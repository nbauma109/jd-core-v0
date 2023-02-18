package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class JavaLangImportTest extends AbstractTestCase {

    @Test
    public void test() throws Exception {
        String output = decompile("jd/core/test/JavaLangImport");
        assertEquals(IOUtils.toString(getClass().getResource("JavaLangImport.txt"), StandardCharsets.UTF_8), output);
    }
}
