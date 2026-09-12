package jd.core.test;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SealedClassesTest extends AbstractTestCase {
    @Test
    public void testSealedInterfaceAndSubclasses() throws Exception {
        String shape = decompile("jd/core/test/sealedcase/Shape", "17");
        assertContainsHeader(shape, "sealed interface Shape permits Circle, OpenShape, Branch");

        String openShape = decompile("jd/core/test/sealedcase/OpenShape", "17");
        assertContainsHeader(openShape, "non-sealed class OpenShape implements Shape");

        String circle = decompile("jd/core/test/sealedcase/Circle", "17");
        assertContainsHeader(circle, "final class Circle implements Shape");
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

    private static void assertContainsHeader(String output, String header) {
        assertTrue(output, output.replaceAll("\\s+", " ").contains(header));
    }
}
