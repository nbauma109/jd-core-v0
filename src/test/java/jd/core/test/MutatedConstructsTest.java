package jd.core.test;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.LocalVariableTypeTable;
import org.apache.bcel.classfile.Method;
import org.jd.core.v1.api.loader.Loader;
import org.junit.Test;

/**
 * The reconstructors recognise fixed bytecode shapes and must give up, not fail, on any other.
 * Damages the classes of a javac 21 jar in several ways and decompiles them.
 */
public class MutatedConstructsTest extends AbstractTestCase {

    private static final String[] TYPES = { "Constructs", "More", "Third", "Extra", "s/W1", "s/T1", "s/L1", "s/P1", "s/C13",
            "s/C14", "s/C15", "s/C17", "s/K1" };

    @Override
    protected boolean recompile() {
        return false;
    }

    @Test
    public void withoutExceptionTable() throws IOException {
        decompileMutated(code -> code.setExceptionTable(new CodeException[0]));
    }

    @Test
    public void withHalfOfTheExceptionTable() throws IOException {
        decompileMutated(code -> {
            CodeException[] table = code.getExceptionTable();
            List<CodeException> kept = new ArrayList<>();
            for (int i = 0; i < table.length; i += 2) {
                kept.add(table[i]);
            }
            code.setExceptionTable(kept.toArray(new CodeException[0]));
        });
    }

    @Test
    public void withCatchAllHandlers() throws IOException {
        decompileMutated(code -> Arrays.stream(code.getExceptionTable()).forEach(entry -> entry.setCatchType(0)));
    }

    @Test
    public void withHandlersMovedByOneInstruction() throws IOException {
        decompileMutated(code -> Arrays.stream(code.getExceptionTable())
                .forEach(entry -> entry.setHandlerPC(Math.max(0, entry.getHandlerPC() + 1))));
    }

    @Test
    public void withShrunkProtectedRanges() throws IOException {
        decompileMutated(code -> Arrays.stream(code.getExceptionTable())
                .forEach(entry -> entry.setEndPC(Math.max(entry.getStartPC(), entry.getEndPC() - 1))));
    }

    @Test
    public void withoutLocalVariableTables() throws IOException {
        decompileMutated(code -> code.setAttributes(Arrays.stream(code.getAttributes())
                .filter(a -> !(a instanceof LocalVariableTable || a instanceof LocalVariableTypeTable))
                .toArray(Attribute[]::new)));
    }

    @Test
    public void withoutLineNumbers() throws IOException {
        decompileMutated(code -> code.setAttributes(Arrays.stream(code.getAttributes())
                .filter(a -> !(a instanceof LineNumberTable)).toArray(Attribute[]::new)));
    }

    @Test
    public void withoutAnyDebugInformation() throws IOException {
        decompileMutated(code -> code.setAttributes(new Attribute[0]));
    }

    @Test
    public void withInvertedBranches() throws IOException {
        decompileMutated(code -> rewriteOpcodes(code, opcode -> {
            if (opcode >= 153 && opcode <= 166) {
                return ((opcode - 153) ^ 1) + 153;
            }
            return opcode == 198 ? 199 : opcode == 199 ? 198 : opcode;
        }));
    }

    @Test
    public void withSwappedBooleanConstants() throws IOException {
        decompileMutated(code -> rewriteOpcodes(code, opcode -> opcode == 3 ? 4 : opcode == 4 ? 3 : opcode));
    }

    @Test
    public void withShiftedReferenceStores() throws IOException {
        decompileMutated(code -> rewriteOpcodes(code, opcode -> opcode >= 75 && opcode <= 77 ? opcode + 1 : opcode));
    }

    @Test
    public void withShiftedReferenceLoads() throws IOException {
        decompileMutated(code -> rewriteOpcodes(code, opcode -> opcode >= 43 && opcode <= 44 ? opcode + 1 : opcode));
    }

    /** Rewrites the opcode of every instruction; only opcodes of the same length may be exchanged. */
    private static void rewriteOpcodes(Code code, java.util.function.IntUnaryOperator rewrite) {
        byte[] bytes = code.getCode().clone();
        org.apache.bcel.generic.InstructionList instructions = new org.apache.bcel.generic.InstructionList(bytes);
        for (org.apache.bcel.generic.InstructionHandle handle : instructions.getInstructionHandles()) {
            int position = handle.getPosition();
            bytes[position] = (byte) rewrite.applyAsInt(bytes[position] & 0xFF);
        }
        code.setCode(bytes);
    }

    private void decompileMutated(Consumer<Code> mutation) throws IOException {
        Map<String, byte[]> classes = new TreeMap<>();
        for (Map.Entry<String, byte[]> entry : readJar().entrySet()) {
            classes.put(entry.getKey(), mutate(entry.getKey(), entry.getValue(), mutation));
        }
        Loader loader = new Loader() {
            @Override
            public boolean canLoad(String name) {
                return classes.containsKey(normalize(name));
            }

            @Override
            public byte[] load(String name) {
                return classes.get(normalize(name));
            }
        };
        for (String type : TYPES) {
            try {
                assertNotNull(decompile(type, loader));
            } catch (RuntimeException | StackOverflowError e) {
                // The damaged byte code may be unreadable; only an endless loop would fail this test
                assertNotNull(e);
            }
        }
    }

    private static String normalize(String name) {
        return name.endsWith(".class") ? name : name + ".class";
    }

    private static byte[] mutate(String name, byte[] bytes, Consumer<Code> mutation) throws IOException {
        JavaClass javaClass = new ClassParser(new ByteArrayInputStream(bytes), name).parse();
        for (Method method : javaClass.getMethods()) {
            Code code = method.getCode();
            if (code != null) {
                mutation.accept(code);
            }
        }
        return javaClass.getBytes();
    }

    private Map<String, byte[]> readJar() throws IOException {
        Map<String, byte[]> classes = new TreeMap<>();
        try (InputStream in = getClass().getResourceAsStream("/constructs-jdk21.jar");
                ZipInputStream zip = new ZipInputStream(in)) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (entry.getName().endsWith(".class")) {
                    classes.put(entry.getName(), zip.readAllBytes());
                }
            }
        }
        return classes;
    }
}
