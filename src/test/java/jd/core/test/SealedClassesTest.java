package jd.core.test;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SealedClassesTest extends AbstractTestCase {
    @Test
    public void testSealedInterfaceAndSubclasses() throws Exception {
        String shape = decompile("jd/core/test/sealedcase/Shape", "17");
        assertContainsHeader(shape, "sealed interface Shape permits Circle, OpenShape, Branch, OpenType, Point");

        String openShape = decompile("jd/core/test/sealedcase/OpenShape", "17");
        assertContainsHeader(openShape, "non-sealed class OpenShape implements Shape");

        String circle = decompile("jd/core/test/sealedcase/Circle", "17");
        assertContainsHeader(circle, "final class Circle implements Shape");

        String openType = decompile("jd/core/test/sealedcase/OpenType", "17");
        assertContainsHeader(openType, "non-sealed interface OpenType extends Shape");

        String point = decompile("jd/core/test/sealedcase/Point", "17");
        assertContainsHeader(point, "record Point() implements Shape");
    }

    @Test
    public void testSealedClassWithInterfacesAndPermits() throws Exception {
        String branch = decompile("jd/core/test/sealedcase/Branch", "17");
        assertContainsHeader(branch, "sealed class Branch implements Shape permits Leaf");

        String genericBase = decompile("jd/core/test/sealedcase/GenericBase", "17");
        assertContainsHeader(genericBase, "abstract sealed class GenericBase<T> permits GenericLeaf");

        String genericLeaf = decompile("jd/core/test/sealedcase/GenericLeaf", "17");
        assertContainsHeader(genericLeaf, "final class GenericLeaf extends GenericBase<String>");
    }

    @Test
    public void testImplicitlySealedEnumHasNoExplicitSealedSyntax() throws Exception {
        String output = decompile("jd/core/test/sealedcase/ImplicitlySealedEnum", "17");
        assertContainsHeader(output, "enum ImplicitlySealedEnum");
        assertFalse(output, output.contains("sealed enum"));
        assertFalse(output, output.contains("permits"));
    }

    @Test
    public void testSealedClassInDefaultPackage() throws Exception {
        String output = decompile("DefaultShape", "17");
        assertContainsHeader(output, "sealed class DefaultShape permits DefaultLeaf");
    }

    private static void assertContainsHeader(String output, String header) {
        assertTrue(output, output.replaceAll("\\s+", " ").contains(header));
    }
}
