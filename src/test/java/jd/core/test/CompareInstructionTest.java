package jd.core.test;

import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertTrue;

import jd.core.model.classfile.ClassFile;
import jd.core.model.reference.ReferenceMap;
import jd.core.process.analyzer.classfile.ClassFileAnalyzer;
import jd.core.process.analyzer.classfile.ReferenceAnalyzer;
import jd.core.process.analyzer.instruction.fast.visitor.FastCompareInstructionVisitor;
import jd.core.process.deserializer.ClassFileDeserializer;

public class CompareInstructionTest {

    @Test
    public void test() throws Exception {
        test("jd/core/test/CompareInstruction", new ClassPathLoader());
    }

    @Test
    public void testEcj() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/dotclass14-ecj-1.4.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String internalClassName = "jd/core/test/DotClass14";
            test(internalClassName, loader);
        }
    }

    private static void test(String internalClassPath, Loader loader) {
        ClassFile cf1 = buildClass(internalClassPath, loader);
        ClassFile cf2 = buildClass(internalClassPath, loader);
        FastCompareInstructionVisitor cmp = new FastCompareInstructionVisitor();
        for (int i = 0; i < cf1.getMethods().length; i++) {
            assertTrue(cmp.visit(cf1.getMethods()[i].getFastNodes(), cf2.getMethods()[i].getFastNodes()));
        }            
    }

    private static ClassFile buildClass(String internalClassPath, Loader loader) {
        ClassFile classFile = ClassFileDeserializer.deserialize(loader, internalClassPath);
        ReferenceMap referenceMap = new ReferenceMap();
        ClassFileAnalyzer.analyze(referenceMap, classFile);
        ReferenceAnalyzer.analyze(referenceMap, classFile);
        return classFile;
    }
}
