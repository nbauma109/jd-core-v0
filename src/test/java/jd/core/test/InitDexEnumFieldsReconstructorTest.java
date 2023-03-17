package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class InitDexEnumFieldsReconstructorTest extends AbstractTestCase {

    @Test
    public void test() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/fuelprices-dex2jar.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "gr/androiddev/FuelPrices/StaticTools";
            String output = decompile(internalClassName, loader);
            assertEquals(IOUtils.toString(getClass().getResource("StaticTools.txt"), StandardCharsets.UTF_8), output);
        }
    }
}
