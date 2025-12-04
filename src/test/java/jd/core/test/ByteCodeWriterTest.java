package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

public class ByteCodeWriterTest {

    @Test
    public void testTableSwitch() throws Exception {
        test("jd/core/test/TableSwitch", "tableSwitch", "()V", "TableSwitchByteCode.txt");
    }

    @Test
    public void testLookupSwitch() throws Exception {
        test("jd/core/test/LookupSwitch", "lookupSwitch", "()V", "LookupSwitchByteCode.txt");
    }

    @Test
    public void testInvokeInterfaceIInc() throws Exception {
        test("jd/core/test/ByteCodeInput", "invokeInterfaceIInc", "()V", "InvokeInterfaceIIncByteCode.txt");
    }

    @Test
    public void testMultiANewArray() throws Exception {
        test("jd/core/test/ByteCodeInput", "multiANewArray", "()V", "MultiANewArrayByteCode.txt");
    }

    @Test
    public void testJavac_1_2_2_novar() throws Exception {
        test("javac-1.2.2");
    }

    @Test
    public void testJavac_1_3_1_28_novar() throws Exception {
        test("javac-1.3.1_28");
    }

    private void test(String compiler) throws IOException {
        StringBuilder expectedFileName = new StringBuilder("TryCatchFinallyBRByteCode");
        StringBuilder jarPath = new StringBuilder("/try-catch-finally-");
        jarPath.append(compiler);
        expectedFileName.append('-');
        expectedFileName.append(compiler);
        expectedFileName.append("-novar.txt");
        jarPath.append("-novar");
        expectedFileName.append("");
        jarPath.append(".jar");
        try (InputStream in = getClass().getResourceAsStream(jarPath.toString())) {
            ZipLoader loader = new ZipLoader(in);
            test("jd/core/test/TryCatchFinallyBR", "tryCatchFinally", "(Ljava/io/File;)V", expectedFileName.toString(), loader);
        }
    }

    private void test(String internalClassPath, String methodName, String methodDescriptor, String expectedResultFile) throws IOException {
        test(internalClassPath, methodName, methodDescriptor, expectedResultFile, new ClassPathLoader());
    }

    private void test(String internalClassPath, String methodName, String methodDescriptor, String expectedResultFile, Loader loader) throws IOException {
        ClassFile classFile = ClassFileDeserializer.deserialize(loader, internalClassPath);
        ReferenceMap referenceMap = new ReferenceMap();
        ClassFileAnalyzer.analyze(referenceMap, classFile);
        ReferenceAnalyzer.analyze(referenceMap, classFile);
        List<LayoutBlock> layoutBlockList = new ArrayList<>(1024);
        Preferences preferences = new Preferences();
        int maxLineNumber = ClassFileLayouter.layout(preferences, referenceMap, classFile, layoutBlockList);
        Printer printer = new PrinterImpl(preferences);
        printer.start(maxLineNumber, classFile.getMajorVersion(), classFile.getMinorVersion());
        Method method = classFile.getMethod(methodName, methodDescriptor);
        ByteCodeWriter.write(loader, printer, referenceMap, classFile, method);
        assertEquals(IOUtils.toString(getClass().getResource(expectedResultFile), StandardCharsets.UTF_8).trim(), printer.toString().trim());
    }
}
