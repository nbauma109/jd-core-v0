package jd.core.test;

import org.jd.core.v1.loader.ClassPathLoader;
import org.jd.core.v1.model.javasyntax.type.ObjectType;
import org.junit.Test;

import static org.jd.core.v1.util.StringConstants.JAVA_LANG_OBJECT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import jd.core.model.classfile.ClassFile;
import jd.core.model.classfile.LocalVariables;
import jd.core.model.instruction.bytecode.instruction.AALoad;
import jd.core.model.instruction.bytecode.instruction.AAStore;
import jd.core.model.instruction.bytecode.instruction.AConstNull;
import jd.core.model.instruction.bytecode.instruction.ALoad;
import jd.core.model.instruction.bytecode.instruction.ANewArray;
import jd.core.model.instruction.bytecode.instruction.AStore;
import jd.core.model.instruction.bytecode.instruction.AThrow;
import jd.core.model.instruction.bytecode.instruction.ArrayLength;
import jd.core.model.instruction.bytecode.instruction.ArrayLoadInstruction;
import jd.core.model.instruction.bytecode.instruction.ArrayReference;
import jd.core.model.instruction.bytecode.instruction.AssertInstruction;
import jd.core.model.instruction.bytecode.instruction.AssignmentInstruction;
import jd.core.model.instruction.bytecode.instruction.BinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.CheckCast;
import jd.core.model.instruction.bytecode.instruction.ComplexConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.ConditionalBranchInstruction;
import jd.core.model.instruction.bytecode.instruction.ConstructorReference;
import jd.core.model.instruction.bytecode.instruction.ConvertInstruction;
import jd.core.model.instruction.bytecode.instruction.DConst;
import jd.core.model.instruction.bytecode.instruction.DupLoad;
import jd.core.model.instruction.bytecode.instruction.DupStore;
import jd.core.model.instruction.bytecode.instruction.ExceptionLoad;
import jd.core.model.instruction.bytecode.instruction.FConst;
import jd.core.model.instruction.bytecode.instruction.GetField;
import jd.core.model.instruction.bytecode.instruction.GetStatic;
import jd.core.model.instruction.bytecode.instruction.Goto;
import jd.core.model.instruction.bytecode.instruction.IBinaryOperatorInstruction;
import jd.core.model.instruction.bytecode.instruction.IConst;
import jd.core.model.instruction.bytecode.instruction.IInc;
import jd.core.model.instruction.bytecode.instruction.ILoad;
import jd.core.model.instruction.bytecode.instruction.IStore;
import jd.core.model.instruction.bytecode.instruction.IncInstruction;
import jd.core.model.instruction.bytecode.instruction.InitArrayInstruction;
import jd.core.model.instruction.bytecode.instruction.InstanceOf;
import jd.core.model.instruction.bytecode.instruction.Jsr;
import jd.core.model.instruction.bytecode.instruction.LConst;
import jd.core.model.instruction.bytecode.instruction.LambdaInstruction;
import jd.core.model.instruction.bytecode.instruction.Ldc;
import jd.core.model.instruction.bytecode.instruction.Ldc2W;
import jd.core.model.instruction.bytecode.instruction.LoadInstruction;
import jd.core.model.instruction.bytecode.instruction.MethodReference;
import jd.core.model.instruction.bytecode.instruction.MonitorEnter;
import jd.core.model.instruction.bytecode.instruction.MonitorExit;
import jd.core.model.instruction.bytecode.instruction.MultiANewArray;
import jd.core.model.instruction.bytecode.instruction.New;
import jd.core.model.instruction.bytecode.instruction.NewArray;
import jd.core.model.instruction.bytecode.instruction.Pop;
import jd.core.model.instruction.bytecode.instruction.Ret;
import jd.core.model.instruction.bytecode.instruction.Return;
import jd.core.model.instruction.bytecode.instruction.ReturnAddressLoad;
import jd.core.model.instruction.bytecode.instruction.ReturnInstruction;
import jd.core.model.instruction.bytecode.instruction.StaticMethodReference;
import jd.core.model.instruction.bytecode.instruction.TernaryOpStore;
import jd.core.model.instruction.bytecode.instruction.TernaryOperator;
import jd.core.model.instruction.bytecode.instruction.UnaryOperatorInstruction;
import jd.core.model.instruction.fast.instruction.FastDeclaration;
import jd.core.model.instruction.fast.instruction.FastInstruction;
import jd.core.model.instruction.fast.instruction.FastLabel;
import jd.core.model.instruction.fast.instruction.FastList;
import jd.core.model.instruction.fast.instruction.FastSwitch;
import jd.core.process.deserializer.ClassFileDeserializer;

public class NullClassFileTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testIIncThrowsUnsupportedOperationException() {
        IInc iinc = new IInc(0, 0, 0, 0, 0);
        iinc.getReturnedSignature(null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testArrayReferenceThrowsUnsupportedOperationException() {
        ArrayReference arrayReference = new ArrayReference(0, 0, 0, null, null);
        arrayReference.getReturnedSignature(null, null);
    }

    @Test
    public void testReturnedSignature() throws Exception {
        ClassPathLoader loader = new ClassPathLoader();
        ClassFile classFile = ClassFileDeserializer.deserialize(loader, JAVA_LANG_OBJECT);
        ALoad aLoad = new ALoad(0, 0, 0, 0);
        ILoad iLoad = new ILoad(0, 0, 0, 0);
        assertNull(new AALoad(0, 0, 0, aLoad, iLoad).getReturnedSignature(null, null));
        assertNull(new AAStore(0, 0, 0, iLoad, null, null).getReturnedSignature(null, null));
        assertNull(new AConstNull(0, 0, 0).getReturnedSignature(null, null));
        assertNull(aLoad.getReturnedSignature(null, null));
        assertNull(aLoad.getReturnedSignature(null, new LocalVariables()));
        assertNull(aLoad.getReturnedSignature(classFile, null));
        ANewArray aNewArray = new ANewArray(0, 0, 0, 0, null);
        assertNull(aNewArray.getReturnedSignature(null, null));
        assertEquals("I", new ArrayLength(0, 0, 0, null).getReturnedSignature(null, null));
        assertNull(new ArrayLoadInstruction(0, 0, 0, null, null, null).getReturnedSignature(null, null));
        assertNull(new AssertInstruction(0, 0, 0, null, null).getReturnedSignature(null, null));
        assertNull(new AssignmentInstruction(0, 0, 0, 0, null, aLoad, aLoad).getReturnedSignature(null, null));
        assertNull(new AStore(0, 0, 0, 0, aLoad).getReturnedSignature(null, null));
        assertNull(new AThrow(0, 0, 0, aLoad).getReturnedSignature(null, null));
        assertNull(new BinaryOperatorInstruction(0, 0, 0, 0, null, null, null, null).getReturnedSignature(null, null));
        assertNull(new CheckCast(0, 0, 0, 0, null).getReturnedSignature(null, null));
        assertNull(new ComplexConditionalBranchInstruction(0, 0, 0, 0, null, 0).getReturnedSignature(null, null));
        assertEquals("Z", new ConditionalBranchInstruction(0, 0, 0, 0, 0).getReturnedSignature(null, null));
        assertNull(new ConstructorReference(0, 0, 0, null, null).getReturnedSignature(null, null));
        assertNull(new ConvertInstruction(0, 0, 0, null, null).getReturnedSignature(null, null));
        assertEquals("D", new DConst(0, 0, 0, 0).getReturnedSignature(null, null));
        DupStore dupStore = new DupStore(0, 0, 0, aLoad);
        assertNull(new DupLoad(0, 0, 0, dupStore).getReturnedSignature(null, null));
        assertNull(dupStore.getReturnedSignature(null, null));
        assertNull(new ExceptionLoad(0, 0, 0, 0).getReturnedSignature(null, null));
        assertEquals("F", new FConst(0, 0, 0, 0).getReturnedSignature(null, null));
        assertNull(new GetField(0, 0, 0, 0, null).getReturnedSignature(null, null));
        assertNull(new GetStatic(0, 0, 0, 0).getReturnedSignature(null, null));
        assertNull(new Goto(0, 0, 0, 0).getReturnedSignature(null, null));
        assertNull(new IBinaryOperatorInstruction(0, 0, 0, 0, null, aLoad, aLoad).getReturnedSignature(null, null));
        assertEquals("X", new IConst(0, 0, 0, 0).getReturnedSignature(null, null));
        assertNull(iLoad.getReturnedSignature(null, null));
        assertNull(new IncInstruction(0, 0, 0, null, 0).getReturnedSignature(null, null));
        assertNull(new InitArrayInstruction(0, 0, 0, aNewArray, null).getReturnedSignature(null, null));
        assertEquals("Z", new InstanceOf(0, 0, 0, 0, null).getReturnedSignature(null, null));
        assertNull(new IStore(0, 0, 0, 0, iLoad).getReturnedSignature(null, null));
        assertNull(new Jsr(0, 0, 0, 0).getReturnedSignature(null, null));
        assertEquals("Z", new LambdaInstruction(0, 0, 0, null, ObjectType.TYPE_PRIMITIVE_BOOLEAN, null, null).getReturnedSignature(null, null));
        assertEquals("J", new LConst(0, 0, 0, 0).getReturnedSignature(null, null));
        assertNull(new Ldc(0, 0, 0, 0).getReturnedSignature(null, null));
        assertNull(new Ldc2W(0, 0, 0, 0).getReturnedSignature(null, null));
        assertNull(new LoadInstruction(0, 0, 0, 0, null).getReturnedSignature(null, null));
        assertNull(new MethodReference(0, 0, 0, null, null, null, null).getReturnedSignature(null, null));
        assertNull(new MonitorEnter(0, 0, 0, null).getReturnedSignature(null, null));
        assertNull(new MonitorExit(0, 0, 0, null).getReturnedSignature(null, null));
        assertNull(new MultiANewArray(0, 0, 0, 0, null).getReturnedSignature(null, null));
        assertNull(new New(0, 0, 0, 0).getReturnedSignature(null, null));
        assertNull(new NewArray(0, 0, 0, 0, null).getReturnedSignature(null, null));
        assertNull(new Pop(0, 0, 0, null).getReturnedSignature(null, null));
        assertNull(new Ret(0, 0, 0, 0).getReturnedSignature(null, null));
        assertNull(new Return(0, 0, 0).getReturnedSignature(null, null));
        assertNull(new ReturnAddressLoad(0, 0, 0).getReturnedSignature(null, null));
        assertNull(new ReturnInstruction(0, 0, 0, null, null).getReturnedSignature(null, null));
        assertNull(new StaticMethodReference(0, 0, 0, null, null, null, null).getReturnedSignature(null, null));
        assertNull(new jd.core.model.instruction.bytecode.instruction.Switch(0, 0, 0, null, 0, null).getReturnedSignature(null, null));
        assertNull(new TernaryOperator(0, 0, 0, null, aLoad, aLoad).getReturnedSignature(null, null));
        assertNull(new TernaryOpStore(0, 0, 0, aLoad, 0).getReturnedSignature(null, null));
        assertNull(new UnaryOperatorInstruction(0, 0, 0, 0, null, null, null).getReturnedSignature(null, null));
        assertNull(new FastDeclaration(0, 0, null, null).getReturnedSignature(null, null));
        assertNull(new FastInstruction(0, 0, 0, null).getReturnedSignature(null, null));
        assertNull(new FastLabel(0, 0, 0, aLoad).getReturnedSignature(null, null));
        assertNull(new FastList(0, 0, 0, 0, null).getReturnedSignature(null, null));
        assertNull(new FastSwitch(0, 0, 0, 0, null, null).getReturnedSignature(null, null));
    }
}
