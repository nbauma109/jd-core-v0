package jd.core.test;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class EnumWithoutValuesArraySwitchTest extends AbstractTestCase {
    private static final String DATA_ELEMENT = "jd/core/test/SwitchEnumInnerClass$DataElement";

    @Override
    protected boolean recompile() {
        // The enum is deliberately stripped of its values array, so its source cannot compile.
        return false;
    }

    @Test
    public void caseLabelsFallBackToFieldOrderWithoutValuesArray() throws IOException {
        Loader classPathLoader = new ClassPathLoader();
        byte[] original = classPathLoader.load(DATA_ELEMENT);
        JavaClass dataElement = new ClassParser(new ByteArrayInputStream(original), DATA_ELEMENT + ".class").parse();
        Field[] fields = Arrays.stream(dataElement.getFields())
                .filter(field -> !"$VALUES".equals(field.getName()))
                .toArray(Field[]::new);
        dataElement.setFields(fields);
        byte[] withoutValues = dataElement.getBytes();

        Loader loader = new Loader() {
            @Override
            public boolean canLoad(String name) {
                return classPathLoader.canLoad(name);
            }

            @Override
            public byte[] load(String name) throws IOException {
                return name.startsWith(DATA_ELEMENT + ".") ? withoutValues : classPathLoader.load(name);
            }
        };

        String output = decompile("jd/core/test/SwitchEnumInnerClass", loader);
        assertTrue(output, output.contains("case KEY:"));
        assertTrue(output, output.contains("case VALUE:"));
    }
}
