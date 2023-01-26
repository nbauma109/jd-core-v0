package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class DotClass118Test extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/dotclass118-jdk1.2.2.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/DotClass118";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("DotClass118.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Override
    protected boolean showMetaData() {
        return true;
    }
}
