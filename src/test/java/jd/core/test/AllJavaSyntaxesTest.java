package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class AllJavaSyntaxesTest extends AbstractTestCase {
    @Test
    public void testAllJavaSyntaxes() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/all-java-syntaxes.jar")) {
            Loader loader = new ZipLoader(in);
            String output = decompile("demo/AllJavaSyntaxes", loader);
            String expected = IOUtils.toString(getClass().getResource("AllJavaSyntaxes.txt"), StandardCharsets.UTF_8);
            assertEquals(normalizeLineEndings(expected), normalizeLineEndings(output));
        }
    }

    @Test
    public void testAllJavaSyntaxes2() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/all-java-syntaxes-2.0.jar")) {
            Loader loader = new ZipLoader(in);
            String output = decompile("demo/AllJavaSyntaxes", loader);
            String expected = IOUtils.toString(getClass().getResource("AllJavaSyntaxes2.txt"), StandardCharsets.UTF_8);
            assertEquals(normalizeLineEndings(expected), normalizeLineEndings(output));
        }
    }

    private static String normalizeLineEndings(String value) {
        return value.replace("\r\n", "\n").replace("\r", "\n");
    }

    @Override
    protected boolean recompile() {
        return false;
    }

    @Override
    protected boolean realignmentLineNumber() {
        return true;
    }

    @Override
    protected boolean showLineNumbers() {
        return false;
    }

    @Override
    protected boolean showMetaData() {
        return true;
    }
}
