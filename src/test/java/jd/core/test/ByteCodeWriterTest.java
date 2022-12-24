package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Test;

import java.io.IOException;
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
        test("jd/core/test/TableSwitch", "tableSwitch", "(I)V", "TableSwitchByteCode.txt");
    }

    @Test
    public void testLookupSwitch() throws Exception {
        test("jd/core/test/LookupSwitch", "lookupSwitch", "(I)V", "LookupSwitchByteCode.txt");
    }

    @Test
    public void testInvokeInterfaceIInc() throws Exception {
        test("jd/core/test/ByteCodeInput", "invokeInterfaceIInc", "()V", "InvokeInterfaceIIncByteCode.txt");
    }

    @Test
    public void testMultiANewArray() throws Exception {
        test("jd/core/test/ByteCodeInput", "multiANewArray", "()V", "MultiANewArrayByteCode.txt");
    }

    private void test(String internalClassPath, String methodName, String methodDescriptor, String expectedResultFile) throws IOException {
        Loader loader = new ClassPathLoader();
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
