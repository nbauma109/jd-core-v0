package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class Dup2X2ExampleCreatorTest extends AbstractTestCase {

    @Test
    public void test() throws Exception {
        Dup2X2ExampleCreator creator = new Dup2X2ExampleCreator();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        creator.create(out);
        Loader loader = new ClassPathLoader() {

            @Override
            public byte[] load(String internalName) throws IOException {
                return "jd/core/test/Dup2X2Example.class".equals(internalName) ? out.toByteArray() : super.load(internalName);
            }

            @Override
            public boolean canLoad(String internalName) {
                return "jd/core/test/Dup2X2Example.class".equals(internalName) || super.canLoad(internalName);
            }
        };
        String output = decompile("jd/core/test/Dup2X2Example", loader);
        assertEquals(IOUtils.toString(getClass().getResource("Dup2X2Example.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void testDouble() throws Exception {
        Dup2X2DoubleExampleCreator creator = new Dup2X2DoubleExampleCreator();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        creator.create(out);
        Loader loader = new ClassPathLoader() {

            @Override
            public byte[] load(String internalName) throws IOException {
                return "jd/core/test/Dup2X2Example.class".equals(internalName) ? out.toByteArray() : super.load(internalName);
            }

            @Override
            public boolean canLoad(String internalName) {
                return "jd/core/test/Dup2X2Example.class".equals(internalName) || super.canLoad(internalName);
            }
        };
        String output = decompile("jd/core/test/Dup2X2Example", loader);
        assertEquals(IOUtils.toString(getClass().getResource("Dup2X2DoubleExample.txt"), StandardCharsets.UTF_8), output);
    }
}
