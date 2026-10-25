package jd.core.test;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.io.IOUtils;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ReorderedEnumFieldsSwitchTest extends AbstractTestCase {
    private static final String DIGIT = "jd/core/test/SwitchEnumInnerClass$DataElement";

    @Test
    public void caseLabelsFollowOrdinalsWhenFieldTableIsReordered() throws IOException {
        Loader classPathLoader = new ClassPathLoader();
        byte[] original = classPathLoader.load(DIGIT);
        JavaClass digit = new ClassParser(new ByteArrayInputStream(original), DIGIT + ".class").parse();
        Field[] fields = digit.getFields();
        int first = -1;
        int second = -1;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isEnum()) {
                if (first < 0) {
                    first = i;
                } else if (second < 0) {
                    second = i;
                }
            }
        }
        Collections.swap(Arrays.asList(fields), first, second);
        digit.setFields(fields);
        byte[] reordered = digit.getBytes();

        Loader loader = new Loader() {
            @Override
            public boolean canLoad(String name) {
                return classPathLoader.canLoad(name);
            }

            @Override
            public byte[] load(String name) throws IOException {
                return name.startsWith(DIGIT + ".") ? reordered : classPathLoader.load(name);
            }
        };

        String output = decompile("jd/core/test/SwitchEnumInnerClass", loader);
        assertEquals(IOUtils.toString(getClass().getResource("SwitchEnumInnerClass.txt"), StandardCharsets.UTF_8), output);
    }
}
