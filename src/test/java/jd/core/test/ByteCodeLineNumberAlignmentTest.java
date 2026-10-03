package jd.core.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.Const;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Test;

import jd.core.model.layout.block.LayoutBlock;
import jd.core.model.reference.ReferenceMap;
import jd.core.preferences.Preferences;
import jd.core.printer.PrinterImpl;
import jd.core.process.analyzer.classfile.ClassFileAnalyzer;
import jd.core.process.analyzer.classfile.ReferenceAnalyzer;
import jd.core.process.deserializer.ClassFileDeserializer;
import jd.core.process.layouter.ClassFileLayouter;
import jd.core.process.writer.ClassFileWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ByteCodeLineNumberAlignmentTest {
    @Test
    public void oversizedDisassemblyKeepsAllInstructionsAndFollowingMethodAligned() {
        ClassGen generated = new ClassGen("ByteCodeAlignment", "java.lang.Object",
                "ByteCodeAlignment.java", Const.ACC_PUBLIC | Const.ACC_SUPER, null);
        var constants = generated.getConstantPool();
        InstructionList code = new InstructionList();
        InstructionHandle first = code.append(new NOP());
        for (int i = 1; i < 200; i++) {
            code.append(new NOP());
        }
        InstructionHandle last = code.append(org.apache.bcel.generic.InstructionConst.RETURN);
        MethodGen failed = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC, Type.VOID,
                Type.NO_ARGS, new String[0], "failed", "ByteCodeAlignment", code, constants);
        failed.addLineNumber(first, 10);
        failed.addLineNumber(last, 12);
        failed.setMaxStack();
        failed.setMaxLocals();
        generated.addMethod(failed.getMethod());

        InstructionList followingCode = new InstructionList();
        InstructionHandle value = followingCode.append(new PUSH(constants, 42));
        followingCode.append(org.apache.bcel.generic.InstructionConst.IRETURN);
        MethodGen following = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC, Type.INT,
                Type.NO_ARGS, new String[0], "after", "ByteCodeAlignment", followingCode, constants);
        following.addLineNumber(value, 20);
        following.setMaxStack();
        following.setMaxLocals();
        generated.addMethod(following.getMethod());

        byte[] bytes = generated.getJavaClass().getBytes();
        ClassPathLoader fallback = new ClassPathLoader();
        Loader loader = new Loader() {
            public boolean canLoad(String name) {
                return "ByteCodeAlignment.class".equals(name) || fallback.canLoad(name);
            }
            public byte[] load(String name) throws java.io.IOException {
                return "ByteCodeAlignment.class".equals(name) ? bytes : fallback.load(name);
            }
        };
        var classFile = ClassFileDeserializer.deserialize(loader, "ByteCodeAlignment.class");
        ReferenceMap references = new ReferenceMap();
        ClassFileAnalyzer.analyze(references, classFile);
        ReferenceAnalyzer.analyze(references, classFile);
        classFile.getMethods()[0].setContainsError(true);

        Preferences preferences = new Preferences();
        preferences.setRealignmentLineNumber(true);
        preferences.setShowLineNumbers(true);
        preferences.setWriteMetaData(false);
        List<LayoutBlock> blocks = new ArrayList<>();
        int max = ClassFileLayouter.layout(preferences, references, classFile, blocks);
        PrinterImpl printer = new PrinterImpl(preferences);
        ClassFileWriter.write(loader, printer, references, max,
                classFile.getMajorVersion(), classFile.getMinorVersion(), blocks);
        String output = printer.toString();

        AbstractTestCase.assertRealignedLineNumbers("ByteCodeAlignment", output);
        assertTrue(output, output.contains("/* 20 */     return 42;"));
        assertEquals(output, 0, printer.getMisalignedLineNumberCount());
        for (int offset = 0; offset < 200; offset++) {
            assertTrue("Missing bytecode offset " + offset + "\n" + output,
                    java.util.regex.Pattern.compile("(?<![0-9])" + offset + ": nop").matcher(output).find());
        }
        assertTrue(output, output.contains("200: return"));
    }
}
