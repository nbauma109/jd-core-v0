package jd.core.test;

import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

import jd.core.VersionAware;

public class MetaDataTest extends AbstractTestCase implements VersionAware {

    @Test
    public void test490() throws Exception {
        String output = decompile("org/junit/Test");
        assertEquals(getVersion(), getProperty(output, "JD-Core Version:"));
        assertEquals("5 (49.0)", getProperty(output, "Java compiler version:"));
    }

    @Test
    public void test453() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/dotclass118-jdk1.2.2.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/DotClass118";
            String output = decompile(internalClassName, loader);
            assertEquals("1 (45.3)", getProperty(output, "Java compiler version:"));
        }
    }

    private static String getProperty(String output, String property) {
        int beginIdx = output.indexOf(property);
        int endIdx = output.indexOf('\n', beginIdx);
        return output.substring(beginIdx + property.length(), endIdx).trim();
    }

    @Override
    protected boolean showMetaData() {
        return true;
    }
}
