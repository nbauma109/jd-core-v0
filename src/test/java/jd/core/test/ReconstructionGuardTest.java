package jd.core.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.Const;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.Type;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Test;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.ConstantPool;
import jd.core.model.classfile.Field;
import jd.core.model.classfile.Method;
import jd.core.model.classfile.LocalVariables;
import jd.core.process.analyzer.classfile.ClassFileAnalyzer;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.AConstNull;
import jd.core.model.instruction.bytecode.instruction.Invokespecial;
import jd.core.model.instruction.bytecode.instruction.PutField;
import jd.core.model.instruction.bytecode.instruction.Return;
import jd.core.model.instruction.bytecode.instruction.Invokevirtual;
import jd.core.model.instruction.bytecode.instruction.Pop;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.ByteCodeConstants;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.AssertInstruction;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.IfInstruction;
import jd.core.model.instruction.bytecode.instruction.Instruction;
import jd.core.model.instruction.bytecode.instruction.InvokeNew;
import jd.core.model.instruction.fast.FastConstants;
import jd.core.model.instruction.fast.instruction.FastTestList;
import jd.core.process.analyzer.instruction.fast.reconstructor.StructuredAssertReconstructor;
import jd.core.process.analyzer.instruction.fast.reconstructor.EmptyEnumSwitchReconstructor;
import jd.core.process.analyzer.instruction.bytecode.reconstructor.AssertInstructionReconstructor;
import jd.core.model.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import jd.core.process.analyzer.classfile.AccessorAnalyzer;
import jd.core.model.classfile.accessor.PutStaticAccessor;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.ConvertInstruction;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.LoadInstruction;
import jd.core.model.instruction.bytecode.instruction.PutStatic;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;

/** Check recognizer boundaries: ordinary conditionals must retain their meaning. */
public class ReconstructionGuardTest {
    @Test
    public void recoversFalseAssertionAndRetainsItsMessage() {
        ClassFile owner = owner("test/AssertionOwner");
        IConst message = new IConst(ByteCodeConstants.ICONST, 30, 1, 17);
        FastTestList guard = guard(owner, owner.getThisClassName(), "$assertionsDisabled",
                List.of(throwing(owner, "java/lang/AssertionError", List.of(message))));
        List<Instruction> code = new ArrayList<>(List.of(guard));
        new StructuredAssertReconstructor(owner).visit(code);
        assertTrue(code.get(0) instanceof AssertInstruction);
        AssertInstruction assertion = (AssertInstruction) code.get(0);
        assertEquals(0, ((IConst) assertion.getTest()).getValue());
        assertSame(message, assertion.getMsg());
    }

    @Test
    public void leavesNonAssertionThrowAndOrdinaryFlagUntouched() {
        ClassFile owner = owner("test/AssertionOwner");
        assertUnchanged(owner, guard(owner, owner.getThisClassName(), "$assertionsDisabled",
                List.of(throwing(owner, "java/lang/IllegalStateException", List.of()))));
        assertUnchanged(owner, guard(owner, owner.getThisClassName(), "enabled",
                List.of(throwing(owner, "java/lang/AssertionError", List.of()))));
        assertUnchanged(owner, guard(owner, "other/Unrelated", "$assertionsDisabled",
                List.of(throwing(owner, "java/lang/AssertionError", List.of()))));
    }

    @Test
    public void leavesUserDeclaredAssertionNamedFieldUntouched() {
        ClassFile owner = owner("test/AssertionOwner");
        owner.getField("$assertionsDisabled", "Z").setAccessFlags(Const.ACC_STATIC | Const.ACC_FINAL);
        assertUnchanged(owner, guard(owner, owner.getThisClassName(), "$assertionsDisabled",
                List.of(throwing(owner, "java/lang/AssertionError", List.of()))));
    }

    @Test
    public void leavesAdditionalStatementsAndNonThrowingFailureUntouched() {
        ClassFile owner = owner("test/AssertionOwner");
        Instruction error = throwing(owner, "java/lang/AssertionError", List.of());
        assertUnchanged(owner, guard(owner, owner.getThisClassName(), "$assertionsDisabled", List.of()));
        assertUnchanged(owner, guard(owner, owner.getThisClassName(), "$assertionsDisabled", List.of(error, error)));
        FastTestList nested = new FastTestList(FastConstants.IF_SIMPLE, 20, 1, 100,
                condition(owner, owner.getThisClassName(), "enabled"), List.of(new IConst(ByteCodeConstants.ICONST, 30, 1, 0)));
        assertUnchanged(owner, guard(owner, owner.getThisClassName(), "$assertionsDisabled", List.of(nested)));
    }

    @Test
    public void leavesLoopsExistingExceptionsAndExtraFailureStatementsUntouched() {
        ClassFile owner = owner("test/AssertionOwner");
        Instruction error = throwing(owner, "java/lang/AssertionError", List.of());
        Instruction existing = new AThrow(Const.ATHROW, 31, 1,
                new jd.core.model.instruction.bytecode.instruction.ALoad(Const.ALOAD, 30, 1, 1));
        assertUnchanged(owner, guard(owner, owner.getThisClassName(), "$assertionsDisabled", List.of(existing)));
        for (List<Instruction> body : List.of(List.<Instruction>of(), List.of(error, error), List.of(existing))) {
            FastTestList failure = new FastTestList(FastConstants.IF_SIMPLE, 20, 1, 100,
                    condition(owner, owner.getThisClassName(), "enabled"), body);
            assertUnchanged(owner, guard(owner, owner.getThisClassName(), "$assertionsDisabled", List.of(failure)));
        }
        FastTestList loop = new FastTestList(FastConstants.WHILE, 20, 1, 100,
                condition(owner, owner.getThisClassName(), "enabled"), List.of(error));
        assertUnchanged(owner, guard(owner, owner.getThisClassName(), "$assertionsDisabled", List.of(loop)));
    }

    @Test
    public void doesNotInvertEnabledGuardsOrParameterConditions() {
        ClassFile owner = owner("test/AssertionOwner");
        Instruction error = throwing(owner, "java/lang/AssertionError", List.of());
        FastTestList enabled = guard(owner, owner.getThisClassName(), "$assertionsDisabled", List.of(error));
        ((IfInstruction) enabled.getTest()).setCmp(ByteCodeConstants.CMP_NE);
        assertUnchanged(owner, enabled);
        FastTestList constant = new FastTestList(FastConstants.IF_SIMPLE, 10, 1, 100,
                new IConst(ByteCodeConstants.ICONST, 9, 1, 0), List.of(error));
        assertUnchanged(owner, constant);
        FastTestList parameter = guard(owner, owner.getThisClassName(), "$assertionsDisabled", List.of(error));
        ((IfInstruction) parameter.getTest()).setValue(new IConst(ByteCodeConstants.ICONST, 9, 1, 0));
        assertUnchanged(owner, parameter);
        FastTestList nested = new FastTestList(FastConstants.IF_SIMPLE, 20, 1, 100,
                condition(owner, owner.getThisClassName(), "enabled"),
                List.of(throwing(owner, "java/lang/IllegalStateException", List.of())));
        assertUnchanged(owner, guard(owner, owner.getThisClassName(), "$assertionsDisabled", List.of(nested)));
    }

    @Test
    public void resolvesNearestSwitchMapOwnerAndMissingMaps() {
        ClassFile outer = owner("test/AssertionOwner");
        ClassFile inner = owner("test/AssertionOwner$Inner");
        inner.setOuterClass(outer);
        assertNull(inner.getSwitchMapOwner("missing"));
        outer.getSwitchMaps().put("table", List.of(1));
        assertSame(outer, inner.getSwitchMapOwner("table"));
        inner.getSwitchMaps().put("table", List.of(2));
        assertSame(inner, inner.getSwitchMapOwner("table"));
    }

    @Test
    public void constructorStoresOfNullAndThisAreNotCapturedParameters() {
        ClassFile owner = owner("test/AssertionOwner");
        ConstantPool constants = owner.getConstantPool();
        int member = fieldReference(owner, "syntheticValue", "Ljava/lang/Object;");
        Method constructor = owner.getMethod("<init>", "()V");
        int base = constants.addConstantClass(constants.addConstantUtf8("java/lang/Object"));
        int init = constants.addConstantNameAndType(constants.addConstantUtf8("<init>"), constants.addConstantUtf8("()V"));
        constructor.setFastNodes(new ArrayList<>(List.of(
                new Invokespecial(Const.INVOKESPECIAL, 1, 1, constants.addConstantMethodref(base, init),
                        new ALoad(Const.ALOAD, 0, 1, 0), List.of()),
                new PutField(Const.PUTFIELD, 5, 1, member, new ALoad(Const.ALOAD, 3, 1, 0),
                        new AConstNull(Const.ACONST_NULL, 4, 1)),
                new PutField(Const.PUTFIELD, 8, 1, member, new ALoad(Const.ALOAD, 6, 1, 0),
                        new ALoad(Const.ALOAD, 7, 1, 0)),
                new Return(Const.RETURN, 9, 1))));
        ClassFileAnalyzer.postAnalyzeSingleMethod(owner, constructor);
        assertFalse(constructor.containsError());
        assertEquals(-1, owner.getField("syntheticValue", "Ljava/lang/Object;")
                .getAnonymousClassConstructorParameterIndex());
    }

    @Test
    public void nonSyntheticArrayLookupsRetainTheirEvaluation() {
        for (boolean knownTable : List.of(false, true)) {
            ClassFile owner = owner("test/AssertionOwner");
            ConstantPool constants = owner.getConstantPool();
            if (knownTable) {
                owner.getSwitchMaps().put("table", List.of(1));
            }
            int selectorType = constants.addConstantClass(constants.addConstantUtf8("test/Selector"));
            int signature = constants.addConstantNameAndType(constants.addConstantUtf8("offset"),
                    constants.addConstantUtf8("()I"));
            Instruction index = new Invokevirtual(Const.INVOKEVIRTUAL, 1, 1,
                    constants.addConstantMethodref(selectorType, signature),
                    new AConstNull(Const.ACONST_NULL, 0, 1), List.of());
            ArrayLoadInstruction lookup = new ArrayLoadInstruction(ByteCodeConstants.ARRAYLOAD, 2, 1,
                    new GetStatic(Const.GETSTATIC, 0, 1, fieldReference(owner, "table", "[I")), index, "I");
            assertDiscardPreserved(owner, lookup);
            assertDiscardPreserved(owner, new ArrayLoadInstruction(ByteCodeConstants.ARRAYLOAD, 2, 1,
                    new AConstNull(Const.ACONST_NULL, 0, 1), index, "I"));
            assertDiscardPreserved(owner, new ArrayLoadInstruction(ByteCodeConstants.ARRAYLOAD, 2, 1,
                    lookup.getArrayref(), new IConst(ByteCodeConstants.ICONST, 1, 1, 0), "I"));
            int parameterized = constants.addConstantNameAndType(constants.addConstantUtf8("offset"),
                    constants.addConstantUtf8("(I)I"));
            Instruction call = new Invokevirtual(Const.INVOKEVIRTUAL, 1, 1,
                    constants.addConstantMethodref(selectorType, parameterized),
                    new AConstNull(Const.ACONST_NULL, 0, 1), List.of(new IConst(ByteCodeConstants.ICONST, 0, 1, 1)));
            assertDiscardPreserved(owner, new ArrayLoadInstruction(ByteCodeConstants.ARRAYLOAD, 2, 1,
                    lookup.getArrayref(), call, "I"));
            assertDiscardPreserved(owner, call);
        }
    }

    private static void assertDiscardPreserved(ClassFile owner, Instruction value) {
        Pop discard = new Pop(Const.POP, 3, 1, value);
        Method method = owner.getMethod("<init>", "()V");
        method.setLocalVariables(new LocalVariables());
        method.setInstructions(new ArrayList<>(List.of(discard, new Return(Const.RETURN, 4, 1))));
        ClassFileAnalyzer.analyzeSingleMethod(null, owner, null, method);
        EmptyEnumSwitchReconstructor.visit(owner, method.getInstructions());
        assertSame(discard, method.getInstructions().get(0));
        assertSame(value, discard.getObjectref());
    }

    @Test
    public void staticAccessorsRequireMatchingExpressionsAndDuplicateValues() {
        for (int variant = 0; variant < 12; variant++) {
            ClassFile owner = owner("test/AssignmentOwner");
            int field = fieldReference(owner, "value", "I");
            Instruction parameter = new LoadInstruction(ByteCodeConstants.LOAD, 1, 1,
                    variant == 1 || variant == 7 ? 1 : 0, "I");
            Instruction value = parameter;
            if (variant >= 3 && variant <= 8) {
                Instruction left = variant == 5 ? new IConst(ByteCodeConstants.ICONST, 0, 1, 10)
                        : new GetStatic(Const.GETSTATIC, 0, 1,
                                variant == 4 ? fieldReference(owner, "other", "I") : field);
                Instruction right = variant == 8
                        ? new ConvertInstruction(ByteCodeConstants.CONVERT, 1, 1, parameter, "J") : parameter;
                value = new BinaryOperatorInstruction(ByteCodeConstants.BINARYOP, 2, 1, 4, "I",
                        variant == 6 ? "==" : "+", left, right);
            } else if (variant == 2) {
                value = new IConst(ByteCodeConstants.ICONST, 1, 1, 17);
            }
            DupStore duplicate = new DupStore(ByteCodeConstants.DUPSTORE, 3, 1, value);
            DupStore other = new DupStore(ByteCodeConstants.DUPSTORE, 3, 1, value);
            Instruction returned = variant == 9 ? parameter : duplicate.getDupLoad2();
            Instruction ending = variant == 11 ? new Return(Const.RETURN, 5, 1)
                    : new ReturnInstruction(ByteCodeConstants.XRETURN, 5, 1, returned, "I");
            List<Instruction> code = new ArrayList<>(List.of(duplicate,
                    new PutStatic(Const.PUTSTATIC, 4, 1, field,
                            variant == 10 ? other.getDupLoad1() : duplicate.getDupLoad1()), ending));
            ConstantPool constants = owner.getConstantPool();
            Method method = new Method(new org.apache.bcel.classfile.Method(Const.ACC_STATIC | Const.ACC_SYNTHETIC,
                    constants.addConstantUtf8("access$000"), constants.addConstantUtf8("(I)I"),
                    new org.apache.bcel.classfile.Attribute[0],
                    new org.apache.bcel.classfile.ConstantPool(new org.apache.bcel.classfile.Constant[1])));
            method.setInstructions(code);
            AccessorAnalyzer.analyze(owner, method);
            var accessor = owner.getAccessor("access$000", "(I)I");
            if (variant == 0 || variant == 3 || variant == 8) {
                assertTrue(accessor instanceof PutStaticAccessor);
                assertEquals(variant == 0 ? "=" : "+=", ((PutStaticAccessor) accessor).operator());
            } else {
                assertNull("Unrecognized assignment variant " + variant, accessor);
            }
        }
    }

    private static int fieldReference(ClassFile owner, String name, String descriptor) {
        ConstantPool constants = owner.getConstantPool();
        int signature = constants.addConstantNameAndType(constants.addConstantUtf8(name),
                constants.addConstantUtf8(descriptor));
        return constants.addConstantFieldref(owner.getThisClassIndex(), signature);
    }

    @Test
    public void terminalAssertionRegionIsRecognizedButForeignFlagsAreNot() {
        ClassFile owner = owner("test/AssertionOwner");
        List<Instruction> empty = new ArrayList<>();
        AssertInstructionReconstructor.reconstruct(owner, empty);
        assertTrue(empty.isEmpty());
        IfInstruction disabled = condition(owner, owner.getThisClassName(), "$assertionsDisabled");
        disabled.setCmp(ByteCodeConstants.CMP_NE);
        IfInstruction check = condition(owner, owner.getThisClassName(), "enabled");
        ComplexConditionalBranchInstruction combined = new ComplexConditionalBranchInstruction(
                ByteCodeConstants.COMPLEXIF, 10, 1, ByteCodeConstants.CMP_OR,
                new ArrayList<>(List.of(disabled, check)), 100);
        List<Instruction> code = new ArrayList<>(List.of(combined,
                throwing(owner, "java/lang/AssertionError", List.of())));
        AssertInstructionReconstructor.reconstruct(owner, code);
        assertTrue(code.get(0) instanceof AssertInstruction);
        assertEquals(1, code.size());
        IfInstruction foreign = condition(owner, "other/Unrelated", "$assertionsDisabled");
        foreign.setCmp(ByteCodeConstants.CMP_NE);
        code = new ArrayList<>(List.of(foreign, throwing(owner, "java/lang/AssertionError", List.of()),
                new Return(Const.RETURN, 100, 1)));
        AssertInstructionReconstructor.reconstruct(owner, code);
        assertSame(foreign, code.get(0));
    }

    private static void assertUnchanged(ClassFile owner, FastTestList guard) {
        List<Instruction> code = new ArrayList<>(List.of(guard));
        new StructuredAssertReconstructor(owner).visit(code);
        assertSame(guard, code.get(0));
    }

    private static FastTestList guard(ClassFile owner, String declaringClass, String name, List<Instruction> body) {
        return new FastTestList(FastConstants.IF_SIMPLE, 10, 1, 100,
                condition(owner, declaringClass, name), new ArrayList<>(body));
    }

    private static IfInstruction condition(ClassFile owner, String declaringClass, String name) {
        ConstantPool constants = owner.getConstantPool();
        int type = constants.addConstantClass(constants.addConstantUtf8(declaringClass));
        int signature = constants.addConstantNameAndType(constants.addConstantUtf8(name), constants.addConstantUtf8("Z"));
        int reference = constants.addConstantFieldref(type, signature);
        return new IfInstruction(ByteCodeConstants.IF, 10, 1, ByteCodeConstants.CMP_EQ,
                new GetStatic(Const.GETSTATIC, 9, 1, reference), 100);
    }

    private static AThrow throwing(ClassFile owner, String type, List<Instruction> arguments) {
        ConstantPool constants = owner.getConstantPool();
        int declaringClass = constants.addConstantClass(constants.addConstantUtf8(type));
        String descriptor = arguments.isEmpty() ? "()V" : "(I)V";
        int signature = constants.addConstantNameAndType(constants.addConstantUtf8("<init>"),
                constants.addConstantUtf8(descriptor));
        InvokeNew value = new InvokeNew(ByteCodeConstants.INVOKENEW, 30, 1,
                constants.addConstantMethodref(declaringClass, signature), arguments);
        return new AThrow(Const.ATHROW, 31, 1, value);
    }

    private static ClassFile owner(String name) {
        ClassGen generated = new ClassGen(name.replace('/', '.'), "java.lang.Object", "AssertionOwner.java",
                Const.ACC_PUBLIC | Const.ACC_SUPER, new String[0]);
        generated.addField(new FieldGen(Const.ACC_STATIC | Const.ACC_FINAL | Const.ACC_SYNTHETIC,
                Type.BOOLEAN, "$assertionsDisabled", generated.getConstantPool()).getField());
        generated.addField(new FieldGen(Const.ACC_STATIC, Type.BOOLEAN, "enabled", generated.getConstantPool()).getField());
        generated.addField(new FieldGen(Const.ACC_FINAL | Const.ACC_SYNTHETIC, Type.OBJECT,
                "syntheticValue", generated.getConstantPool()).getField());
        generated.addField(new FieldGen(Const.ACC_STATIC, new org.apache.bcel.generic.ArrayType(Type.INT, 1),
                "table", generated.getConstantPool()).getField());
        generated.addEmptyConstructor(Const.ACC_PUBLIC);
        var type = generated.getJavaClass();
        Field[] fields = java.util.Arrays.stream(type.getFields()).map(Field::new).toArray(Field[]::new);
        return new ClassFile(0, Const.MAJOR_17, new ConstantPool(type.getConstantPool()), type.getAccessFlags(),
                type.getClassNameIndex(), type.getSuperclassNameIndex(), new int[0], fields,
                java.util.Arrays.stream(type.getMethods()).map(Method::new).toArray(Method[]::new),
                type.getAttributes(), new ClassPathLoader());
    }
}
