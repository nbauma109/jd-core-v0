package jd.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.tools.ToolProvider;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.InstructionConst;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Kotlin builds the array of the values of an enum through a local variable, and keeps in a further synthetic field the
 * result of a method of its library. Both used to fail the decompilation of the whole class.
 */
public class EnumValuesThroughLocalVariableTest extends AbstractTestCase {

    private static final String ENUM = "Channel";
    private static final List<String> VALUES = List.of("CURRENT", "CANDIDATE", "NIGHTLY");

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testValuesStoredThroughLocalVariable() throws IOException {
        ClassPathLoader classPathLoader = new ClassPathLoader();
        byte[] rewritten = rewriteValuesMethod(compileWithJavac());
        Loader loader = new Loader() {
            @Override
            public boolean canLoad(String name) {
                return ENUM.equals(name.replace(".class", "")) || classPathLoader.canLoad(name);
            }

            @Override
            public byte[] load(String name) throws IOException {
                return ENUM.equals(name.replace(".class", "")) ? rewritten : classPathLoader.load(name);
            }
        };

        String output = decompile(ENUM, loader);

        assertFalse(output, output.contains("// Byte code:"));
        int current = output.indexOf("CURRENT,");
        int candidate = output.indexOf("CANDIDATE,");
        assertTrue(output, current >= 0 && candidate > current && output.indexOf("NIGHTLY", candidate) > candidate);
    }

    /** Only javac writes the "$values" method that this test rewrites, whichever compiler builds the tests. */
    private byte[] compileWithJavac() throws IOException {
        Path source = folder.getRoot().toPath().resolve(ENUM + ".java");
        Files.writeString(source, "public enum " + ENUM + " { " + String.join(", ", VALUES) + " }");
        int status = ToolProvider.getSystemJavaCompiler().run(null, null, null, "--release", "17", source.toString());
        assertEquals(0, status);
        return Files.readAllBytes(source.resolveSibling(ENUM + ".class"));
    }

    /** Adds "field = System.lineSeparator();" at the end of the static initializer. */
    private static void initializeFromLibrary(ClassGen classGen, InstructionFactory factory, String field) {
        org.apache.bcel.classfile.Method initializer = findMethod(classGen, "<clinit>");
        MethodGen methodGen = new MethodGen(initializer, classGen.getClassName(), classGen.getConstantPool());
        InstructionList code = methodGen.getInstructionList();
        InstructionHandle end = code.getEnd();
        code.insert(end, factory.createInvoke("java.lang.System", "lineSeparator", Type.STRING, Type.NO_ARGS,
                Const.INVOKESTATIC));
        code.insert(end, factory.createFieldAccess(classGen.getClassName(), field, Type.STRING, Const.PUTSTATIC));
        methodGen.setMaxStack();
        classGen.replaceMethod(initializer, methodGen.getMethod());
    }

    private static org.apache.bcel.classfile.Method findMethod(ClassGen classGen, String name) {
        return Arrays.stream(classGen.getMethods())
                .filter(method -> name.equals(method.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("javac writes no method " + name));
    }

    /** Writes "$values" as "T[] a = new T[3]; a[0] = CURRENT; ... return a;". */
    private static byte[] rewriteValuesMethod(byte[] original) throws IOException {
        try (InputStream in = new ByteArrayInputStream(original)) {
            JavaClass javaClass = new ClassParser(in, ENUM + ".class").parse();
            ClassGen classGen = new ClassGen(javaClass);
            ConstantPoolGen constants = classGen.getConstantPool();
            InstructionFactory factory = new InstructionFactory(classGen, constants);
            ObjectType type = new ObjectType(classGen.getClassName());
            ArrayType arrayType = new ArrayType(type, 1);
            org.apache.bcel.classfile.Method values = findMethod(classGen, "$values");

            InstructionList code = new InstructionList();
            code.append(new PUSH(constants, VALUES.size()));
            code.append(factory.createNewArray(type, (short) 1));
            code.append(InstructionFactory.createStore(arrayType, 0));
            for (int ordinal = 0; ordinal < VALUES.size(); ordinal++) {
                code.append(InstructionFactory.createLoad(arrayType, 0));
                code.append(new PUSH(constants, ordinal));
                code.append(factory.createFieldAccess(classGen.getClassName(), VALUES.get(ordinal), type, Const.GETSTATIC));
                code.append(InstructionConst.AASTORE);
            }
            code.append(InstructionFactory.createLoad(arrayType, 0));
            code.append(InstructionFactory.createReturn(arrayType));

            MethodGen methodGen = new MethodGen(values.getAccessFlags(), arrayType, Type.NO_ARGS, null, values.getName(),
                    classGen.getClassName(), code, constants);
            methodGen.setMaxStack();
            methodGen.setMaxLocals();
            classGen.replaceMethod(values, methodGen.getMethod());
            // A synthetic field without initial value describes no value of the enum
            int synthetic = Const.ACC_PRIVATE | Const.ACC_STATIC | Const.ACC_SYNTHETIC;
            classGen.addField(new FieldGen(synthetic, arrayType, "$UNUSED", constants).getField());
            // Like "$ENTRIES" of Kotlin: initialized last, by a method of a library
            classGen.addField(new FieldGen(synthetic, Type.STRING, "$ENTRIES", constants).getField());
            initializeFromLibrary(classGen, factory, "$ENTRIES");
            // The array of the values is searched among all the synthetic fields: it comes last here
            org.apache.bcel.classfile.Field array = classGen.containsField("$VALUES");
            classGen.removeField(array);
            classGen.addField(array);
            return classGen.getJavaClass().getBytes();
        }
    }
}
