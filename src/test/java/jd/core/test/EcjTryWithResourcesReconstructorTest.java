package jd.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.apache.bcel.classfile.CodeException;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.Method;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.process.analyzer.classfile.LocalVariableAnalyzer;
import jd.core.process.analyzer.classfile.reconstructor.EcjTryWithResourcesReconstructor;
import jd.core.process.analyzer.instruction.bytecode.InstructionListBuilder;
import jd.core.process.deserializer.ClassFileDeserializer;

/** The cleanup of a statement is removed entirely, or not at all when it does not have the expected shape. */
public class EcjTryWithResourcesReconstructorTest {

    private static final String FIXTURE = "jd/core/test/fallbacks/EcjTryWithResources";

    /** A method of the fixture and the number of resources of its statements. */
    private record Sample(String name, int resources) {
    }

    private static final List<Sample> SAMPLES = List.of(new Sample("single", 1), new Sample("returned", 1),
            new Sample("three", 3), new Sample("caught", 2), new Sample("exitsOfTheBody", 1),
            new Sample("sequentialAndNested", 3), new Sample("returnedFromNested", 2), new Sample("loopFollowedByAssignment", 1),
            new Sample("lambda$0", 1));

    @Test
    public void testEveryResourceIsFound() throws IOException {
        for (Sample sample : SAMPLES) {
            assertEquals(sample.name(), sample.resources(), reconstruct(sample.name(), list -> {
            }).getTryResources().size());
        }
    }

    @Test
    public void testMissingInstruction() throws IOException {
        for (Sample sample : SAMPLES) {
            int size = instructionCount(sample.name());
            for (int i = 0; i < size; i++) {
                int removed = i;
                reconstruct(sample.name(), list -> list.remove(removed));
            }
        }
    }

    @Test
    public void testTruncatedInstructions() throws IOException {
        for (Sample sample : SAMPLES) {
            int size = instructionCount(sample.name());
            for (int i = 0; i < size; i++) {
                int length = i;
                reconstruct(sample.name(), list -> list.subList(length, list.size()).clear());
            }
        }
    }

    @Test
    public void testRepeatedInstruction() throws IOException {
        for (Sample sample : SAMPLES) {
            int size = instructionCount(sample.name());
            for (int i = 0; i < size; i++) {
                int repeated = i;
                reconstruct(sample.name(), list -> list.add(repeated, list.get(repeated)));
            }
        }
    }

    @Test
    public void testHandlersAbsentFromTheExceptionTable() throws IOException {
        ClassFile classFile = load();
        Method method = find(classFile, "single");
        List<Instruction> list = build(classFile, method);
        CodeException[] unrelated = Arrays.stream(method.getCodeExceptions())
                .map(e -> new CodeException(e.getStartPC(), e.getEndPC(), e.getHandlerPC() + 1000, 0))
                .toArray(CodeException[]::new);
        method.setCodeExceptions(unrelated);
        List<Instruction> before = List.copyOf(list);

        EcjTryWithResourcesReconstructor.reconstruct(classFile, method, list);

        assertEquals(before, list);
        assertTrue(method.getTryResources().isEmpty());
    }

    private static Method reconstruct(String name, Consumer<List<Instruction>> damage) throws IOException {
        ClassFile classFile = load();
        Method method = find(classFile, name);
        List<Instruction> list = build(classFile, method);
        damage.accept(list);
        List<Instruction> before = List.copyOf(list);
        CodeException[] exceptions = method.getCodeExceptions();

        EcjTryWithResourcesReconstructor.reconstruct(classFile, method, list);

        if (method.getTryResources().isEmpty()) {
            assertEquals(name, before, list);
            assertTrue(name, Arrays.equals(exceptions, method.getCodeExceptions()));
        }
        return method;
    }

    private static int instructionCount(String name) throws IOException {
        ClassFile classFile = load();
        return build(classFile, find(classFile, name)).size();
    }

    private static ClassFile load() throws IOException {
        try (InputStream in = EcjTryWithResourcesReconstructorTest.class
                .getResourceAsStream("/decompilation-fallbacks-ecj17.jar")) {
            return ClassFileDeserializer.deserialize(new ZipLoader(in), FIXTURE);
        }
    }

    private static Method find(ClassFile classFile, String name) {
        return Arrays.stream(classFile.getMethods())
                .filter(method -> name.equals(classFile.getConstantPool().getConstantUtf8(method.getNameIndex())))
                .findFirst()
                .orElseThrow(() -> new AssertionError(name));
    }

    private static List<Instruction> build(ClassFile classFile, Method method) {
        List<Instruction> list = new ArrayList<>();
        List<Instruction> listForAnalyze = new ArrayList<>();
        InstructionListBuilder.build(classFile, method, list, listForAnalyze);
        method.setInstructions(list);
        LocalVariableAnalyzer.analyze(classFile, method, classFile.getVariableNameGenerator(), list, listForAnalyze);
        return list;
    }
}
