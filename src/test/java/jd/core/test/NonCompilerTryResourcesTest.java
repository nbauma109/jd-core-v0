package jd.core.test;

import org.apache.bcel.Const;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IFNULL;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.RETURN;
import org.apache.bcel.generic.Type;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.loader.ClassPathLoader;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Shapes of the closing code of a try-with-resources that no compiler generates, hence the generated classes:
 * javac 11+ tests the resource against null either on both paths or on none, and the exception of the protected
 * region is always stored. The shapes that the compilers do generate are compiled fixtures
 * ({@link TryResourcesTarget8Test}, {@link TryResourcesLookAlikesTest}).
 */
public class NonCompilerTryResourcesTest extends AbstractTestCase {

    private static final ObjectType STREAM = new ObjectType("java.io.InputStream");
    private static final ObjectType THROWABLE = new ObjectType("java.lang.Throwable");

    @Override
    protected boolean recompile() {
        return false;
    }

    private String decompileGenerated(String name, boolean guardNormalPath, boolean guardHandler, boolean popException)
            throws IOException {
        byte[] bytes = generate(name, guardNormalPath, guardHandler, popException);
        Loader classPath = new ClassPathLoader();
        String generated = "jd/core/test/" + name;
        Loader loader = new Loader() {
            @Override
            public boolean canLoad(String internalName) {
                return isGenerated(internalName, generated) || classPath.canLoad(internalName);
            }

            @Override
            public byte[] load(String internalName) throws IOException {
                return isGenerated(internalName, generated) ? bytes : classPath.load(internalName);
            }
        };
        return decompile(generated, loader, "1.8");
    }

    private static boolean isGenerated(String internalName, String generated) {
        return internalName.equals(generated) || internalName.equals(generated + ".class");
    }

    @Test
    public void nullCheckOnlyOnTheNormalPath() throws Exception {
        assertTrue(decompileGenerated("GuardedNormal", true, false, false).contains("try ("));
    }

    @Test
    public void nullCheckOnlyInTheHandler() throws Exception {
        assertTrue(decompileGenerated("GuardedHandler", false, true, false).contains("try ("));
    }

    @Test
    public void exceptionIsPopped() throws Exception {
        assertFalse(decompileGenerated("Popped", false, false, true).contains("try ("));
    }

    /**
     * <pre>
     * r = in;
     * try { r.read(); } catch (Throwable t) { [if (r != null)] { try { r.close(); } catch (Throwable s) { t.addSuppressed(s); } } throw t; }
     * [if (r != null)] r.close();
     * </pre>
     */
    private static byte[] generate(String name, boolean guardNormalPath, boolean guardHandler, boolean popException) {
        String className = "jd.core.test." + name;
        ClassGen cg = new ClassGen(className, "java.lang.Object", name + ".java", Const.ACC_PUBLIC | Const.ACC_SUPER, null);
        cg.setMajor(52);
        cg.setMinor(0);
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg);
        InstructionList il = new InstructionList();
        MethodGen mg = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC, Type.VOID, new Type[] { STREAM },
                new String[] { "in" }, "run", className, il, cp);

        il.append(new ALOAD(0));
        il.append(new ASTORE(1));
        InstructionHandle start = il.append(new ALOAD(1));
        il.append(factory.createInvoke("java.io.InputStream", "read", Type.INT, Type.NO_ARGS, Const.INVOKEVIRTUAL));
        InstructionHandle end = il.append(new POP());

        // Normal path
        BranchInstruction skipNormalClose = null;
        if (guardNormalPath) {
            il.append(new ALOAD(1));
            skipNormalClose = (BranchInstruction) il.append(new IFNULL(null)).getInstruction();
        }
        il.append(new ALOAD(1));
        il.append(factory.createInvoke("java.io.InputStream", "close", Type.VOID, Type.NO_ARGS, Const.INVOKEVIRTUAL));
        BranchInstruction toEnd = new GOTO(null);
        InstructionHandle gotoEnd = il.append(toEnd);

        // Handler of the protected region
        InstructionHandle handler = il.append(popException ? new POP() : new ASTORE(2));
        BranchInstruction skipHandlerClose = null;
        if (guardHandler) {
            il.append(new ALOAD(1));
            skipHandlerClose = (BranchInstruction) il.append(new IFNULL(null)).getInstruction();
        }
        InstructionHandle closeStart = il.append(new ALOAD(1));
        il.append(factory.createInvoke("java.io.InputStream", "close", Type.VOID, Type.NO_ARGS, Const.INVOKEVIRTUAL));
        InstructionHandle closeEnd = il.getEnd();
        BranchInstruction toThrow = new GOTO(null);
        il.append(toThrow);
        InstructionHandle suppressed = il.append(new ASTORE(3));
        il.append(new ALOAD(2));
        il.append(new ALOAD(3));
        il.append(factory.createInvoke("java.lang.Throwable", "addSuppressed", Type.VOID, new Type[] { THROWABLE },
                Const.INVOKEVIRTUAL));
        InstructionHandle throwTarget = il.append(new ALOAD(2));
        il.append(new ATHROW());
        InstructionHandle ret = il.append(new RETURN());

        toEnd.setTarget(ret);
        toThrow.setTarget(throwTarget);
        if (skipHandlerClose != null) {
            skipHandlerClose.setTarget(throwTarget);
        }
        if (skipNormalClose != null) {
            skipNormalClose.setTarget(gotoEnd);
        }

        mg.addExceptionHandler(start, end, handler, THROWABLE);
        mg.addExceptionHandler(closeStart, closeEnd, suppressed, THROWABLE);
        mg.addLocalVariable("r", STREAM, 1, start, ret);
        mg.addLocalVariable("t", THROWABLE, 2, handler.getNext() == null ? handler : handler.getNext(), ret);
        mg.addLocalVariable("s", THROWABLE, 3, suppressed.getNext(), throwTarget);
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.addMethod(mg.getMethod());
        cg.addEmptyConstructor(Const.ACC_PUBLIC);
        return cg.getJavaClass().getBytes();
    }
}
