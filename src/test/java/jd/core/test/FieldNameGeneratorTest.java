package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class FieldNameGeneratorTest extends AbstractTestCase {

    @Test
    public void test() throws Exception {
        NonUniqueFieldNamesCreator nonUniqueFieldNamesCreator = new NonUniqueFieldNamesCreator();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        nonUniqueFieldNamesCreator.create(out);
        Loader loader = new ClassPathLoader() {

            @Override
            public byte[] load(String internalName) throws IOException {
                return "jd/core/test/NonUniqueFieldNames.class".equals(internalName) ? out.toByteArray() : super.load(internalName);
            }

            @Override
            public boolean canLoad(String internalName) {
                return "jd/core/test/NonUniqueFieldNames.class".equals(internalName) || super.canLoad(internalName);
            }
        };
        String output = decompile("jd/core/test/NonUniqueFieldNames", loader);
        assertEquals(IOUtils.toString(getClass().getResource("NonUniqueFieldNames.txt"), StandardCharsets.UTF_8), output);
    }

    @Override
    protected boolean recompile() {
        return false;
    }
}
