package jd.core.test;

import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Test;

import static org.junit.Assert.*;

import jd.core.model.classfile.ClassFile;
import jd.core.model.reference.ReferenceMap;
import jd.core.process.analyzer.classfile.ClassFileAnalyzer;
import jd.core.process.analyzer.classfile.ReferenceAnalyzer;
import jd.core.process.deserializer.ClassFileDeserializer;
import jd.core.process.analyzer.instruction.fast.visitor.FastCompareInstructionVisitor;

public class CompareInstructionTest {

    @Test
    public void test() throws Exception {
        test("jd/core/test/CompareInstruction");
    }

    private static void test(String internalClassPath) {
        ClassFile cf1 = buildClass(internalClassPath);
        ClassFile cf2 = buildClass(internalClassPath);
        FastCompareInstructionVisitor cmp = new FastCompareInstructionVisitor();
        for (int i = 0; i < cf1.getMethods().length; i++) {
            assertTrue(cmp.visit(cf1.getMethods()[i].getFastNodes(), cf2.getMethods()[i].getFastNodes()));
        }            
    }

    private static ClassFile buildClass(String internalClassPath) {
        Loader loader = new ClassPathLoader();
        ClassFile classFile = ClassFileDeserializer.deserialize(loader, internalClassPath);
        ReferenceMap referenceMap = new ReferenceMap();
        ClassFileAnalyzer.analyze(referenceMap, classFile);
        ReferenceAnalyzer.analyze(referenceMap, classFile);
        return classFile;
    }
}
