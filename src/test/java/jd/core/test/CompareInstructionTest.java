package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.layout.block.LayoutBlock;
import jd.core.model.reference.ReferenceMap;
import jd.core.preferences.Preferences;
import jd.core.printer.Printer;
import jd.core.printer.PrinterImpl;
import jd.core.process.analyzer.classfile.ClassFileAnalyzer;
import jd.core.process.analyzer.classfile.ReferenceAnalyzer;
import jd.core.process.deserializer.ClassFileDeserializer;
import jd.core.process.layouter.ClassFileLayouter;
import jd.core.process.writer.ByteCodeWriter;
import jd.core.process.analyzer.instruction.fast.visitor.FastCompareInstructionVisitor;

public class CompareInstructionTest {

    @Test
    public void test() throws Exception {
        test("jd/core/test/CompareInstruction");
    }

    private void test(String internalClassPath) throws IOException {
        ClassFile cf1 = buildClass(internalClassPath);
        ClassFile cf2 = buildClass(internalClassPath);
        FastCompareInstructionVisitor cmp = new FastCompareInstructionVisitor();
        for (int i = 0; i < cf1.getMethods().length; i++) {
            assertTrue(cmp.visit(cf1.getMethods()[i].getFastNodes(), cf2.getMethods()[i].getFastNodes()));
        }            
    }

    private ClassFile buildClass(String internalClassPath) throws IOException {
        Loader loader = new ClassPathLoader();
        ClassFile classFile = ClassFileDeserializer.deserialize(loader, internalClassPath);
        ReferenceMap referenceMap = new ReferenceMap();
        ClassFileAnalyzer.analyze(referenceMap, classFile);
        ReferenceAnalyzer.analyze(referenceMap, classFile);
        return classFile;
    }
}
