package jd.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.reference.ReferenceMap;
import jd.core.preferences.Preferences;
import jd.core.printer.PlainTextPrinter;
import jd.core.process.deserializer.ClassFileDeserializer;
import jd.core.process.writer.ByteCodeWriter;

/** The closing brace of a method written as byte code on a limited number of lines used to be commented out. */
public class ByteCodeCommentTest {

    @Test
    public void testCommentLeavesTheFollowingLineEmpty() throws IOException {
        String[] lines = write(3);

        assertEquals(4, lines.length);
        assertTrue(lines[0], lines[0].contains("// Byte code:"));
        assertEquals("", lines[3].strip());
    }

    @Test
    public void testCommentWithoutFollowingLineIsClosed() throws IOException {
        String[] lines = write(0);

        assertEquals(1, lines.length);
        assertTrue(lines[0], lines[0].strip().startsWith("/* Byte code:"));
        assertTrue(lines[0], lines[0].endsWith(" */"));
    }

    private String[] write(int lineCount) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/switch-case-try-javac17.jar")) {
            ZipLoader loader = new ZipLoader(in);
            ClassFile classFile = ClassFileDeserializer.deserialize(loader,
                    "jd/core/test/fallbacks/SwitchCaseShapes");
            Method method = classFile.getMethods()[classFile.getMethods().length - 1];
            PlainTextPrinter printer = new PlainTextPrinter();
            Preferences preferences = new Preferences();
            preferences.setShowLineNumbers(false);
            printer.setPreferences(preferences);
            printer.start(0, 0, 0);
            printer.startOfLine(PlainTextPrinter.UNKNOWN_LINE_NUMBER);
            ByteCodeWriter.write(loader, printer, new ReferenceMap(), classFile, method, lineCount);
            return printer.toString().split("\\R", -1);
        }
    }
}
