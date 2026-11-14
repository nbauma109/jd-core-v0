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
 * The shapes of try-with-resources are generated: javac 11+ only produces some of them, the others check that
 * code that looks like the closing code of a resource is left alone.
 */
public class GeneratedTryResourcesTest extends AbstractTestCase {

    private static final ObjectType STREAM = new ObjectType("java.io.InputStream");
    private static final ObjectType THROWABLE = new ObjectType("java.lang.Throwable");

    /** How the handler of the protected region differs from the closing code of javac 11+. */
    private enum Handler {
        /** The exception is stored, the resource is closed with addSuppressed, the exception is rethrown. */
        COMPLETE,
        /** The exception is popped instead of being stored. */
        POPPED,
        /** The resource is closed without protection of the close itself. */
        NO_INNER_HANDLER,
        /** The exception that is thrown is the one of the close. */
        OTHER_THROWN,
        /** addSuppressed is called on the exception of the close. */
        SWAPPED_SUPPRESSED,
        /** Another stream is closed. */
        OTHER_RESOURCE,
        /** The close is not a call to close(). */
        NOT_A_CLOSE
    }

    @Override
    protected boolean recompile() {
        return false;
    }

    private String decompileGenerated(String name, boolean guardNormalPath, boolean guardHandler, boolean closeAfterRegion,
            Handler handler) throws IOException {
        byte[] bytes = generate(name, guardNormalPath, guardHandler, closeAfterRegion, handler);
        Loader classPath = new ClassPathLoader();
        Loader loader = new Loader() {
            @Override
            public boolean canLoad(String internalName) {
                return isGenerated(internalName, name) || classPath.canLoad(internalName);
            }

            @Override
            public byte[] load(String internalName) throws IOException {
                return isGenerated(internalName, name) ? bytes : classPath.load(internalName);
            }
        };
        return decompile("jd/core/test/" + name, loader, "1.8");
    }

    private static boolean isGenerated(String internalName, String name) {
        return internalName.equals("jd/core/test/" + name) || internalName.equals("jd/core/test/" + name + ".class");
    }

    @Test
    public void completeShape() throws Exception {
        assertTrue(decompileGenerated("Complete", false, false, true, Handler.COMPLETE).contains("try ("));
    }

    @Test
    public void completeShapeWithNullChecks() throws Exception {
        assertTrue(decompileGenerated("Guarded", true, true, true, Handler.COMPLETE).contains("try ("));
    }

    @Test
    public void nullCheckOnlyOnTheNormalPath() throws Exception {
        assertTrue(decompileGenerated("GuardedNormal", true, false, true, Handler.COMPLETE).contains("try ("));
    }

    @Test
    public void nullCheckOnlyInTheHandler() throws Exception {
        assertTrue(decompileGenerated("GuardedHandler", false, true, true, Handler.COMPLETE).contains("try ("));
    }

    @Test
    public void resourceIsNotClosedAfterTheRegion() throws Exception {
        assertFalse(decompileGenerated("NotClosed", false, false, false, Handler.COMPLETE).contains("try ("));
    }

    @Test
    public void exceptionIsPopped() throws Exception {
        assertFalse(decompileGenerated("Popped", false, false, true, Handler.POPPED).contains("try ("));
    }

    @Test
    public void closeIsNotProtected() throws Exception {
        assertFalse(decompileGenerated("Unprotected", false, false, true, Handler.NO_INNER_HANDLER).contains("try ("));
    }

    @Test
    public void otherExceptionIsThrown() throws Exception {
        assertFalse(decompileGenerated("OtherThrown", false, false, true, Handler.OTHER_THROWN).contains("try ("));
    }

    @Test
    public void suppressedAreSwapped() throws Exception {
        assertFalse(decompileGenerated("Swapped", false, false, true, Handler.SWAPPED_SUPPRESSED).contains("try ("));
    }

    @Test
    public void otherResourceIsClosed() throws Exception {
        assertFalse(decompileGenerated("OtherResource", true, true, true, Handler.OTHER_RESOURCE).contains("try ("));
    }

    @Test
    public void notACloseCall() throws Exception {
        assertFalse(decompileGenerated("NotAClose", false, false, true, Handler.NOT_A_CLOSE).contains("try ("));
    }

    /**
     * <pre>
     * r = in;
     * try { r.read(); } catch (Throwable t) { [if (r != null)] { try { r.close(); } catch (Throwable s) { t.addSuppressed(s); } } throw t; }
     * [if (r != null)] r.close();
     * </pre>
     */
    private static byte[] generate(String name, boolean guardNormalPath, boolean guardHandler, boolean closeAfterRegion,
            Handler handler) {
        String className = "jd.core.test." + name;
        ClassGen cg = new ClassGen(className, "java.lang.Object", name + ".java", Const.ACC_PUBLIC | Const.ACC_SUPER, null);
        cg.setMajor(52);
        cg.setMinor(0);
        ConstantPoolGen cp = cg.getConstantPool();
        InstructionFactory factory = new InstructionFactory(cg);
        InstructionList il = new InstructionList();
        MethodGen mg = new MethodGen(Const.ACC_PUBLIC | Const.ACC_STATIC, Type.VOID, new Type[] { STREAM, STREAM },
                new String[] { "in", "other" }, "run", className, il, cp);

        il.append(new ALOAD(0));
        il.append(new ASTORE(2));
        InstructionHandle start = il.append(new ALOAD(2));
        il.append(factory.createInvoke("java.io.InputStream", "read", Type.INT, Type.NO_ARGS, Const.INVOKEVIRTUAL));
        InstructionHandle end = il.append(new POP());

        BranchInstruction skipNormalClose = null;
        if (closeAfterRegion) {
            if (guardNormalPath) {
                il.append(new ALOAD(2));
                skipNormalClose = (BranchInstruction) il.append(new IFNULL(null)).getInstruction();
            }
            il.append(new ALOAD(2));
            il.append(factory.createInvoke("java.io.InputStream", "close", Type.VOID, Type.NO_ARGS, Const.INVOKEVIRTUAL));
        }
        BranchInstruction toEnd = new GOTO(null);
        InstructionHandle gotoEnd = il.append(toEnd);

        // Handler of the protected region
        InstructionHandle handlerStart;
        if (handler == Handler.POPPED) {
            handlerStart = il.append(new POP());
        } else {
            handlerStart = il.append(new ASTORE(3));
        }
        InstructionHandle throwTarget = null;
        BranchInstruction skipHandlerClose = null;
        if (guardHandler) {
            il.append(new ALOAD(2));
            InstructionHandle guard = il.append(new IFNULL(null));
            skipHandlerClose = (BranchInstruction) guard.getInstruction();
        }
        InstructionHandle closeStart = il.append(new ALOAD(handler == Handler.OTHER_RESOURCE ? 1 : 2));
        if (handler == Handler.NOT_A_CLOSE) {
            il.append(factory.createInvoke("java.io.InputStream", "available", Type.INT, Type.NO_ARGS, Const.INVOKEVIRTUAL));
            il.append(new POP());
        } else {
            il.append(factory.createInvoke("java.io.InputStream", "close", Type.VOID, Type.NO_ARGS, Const.INVOKEVIRTUAL));
        }
        InstructionHandle closeEnd = il.getEnd();
        InstructionHandle suppressed = null;
        BranchInstruction toThrow = new GOTO(null);
        if (handler == Handler.NO_INNER_HANDLER) {
            throwTarget = il.append(new ALOAD(3));
            il.append(new ATHROW());
        } else {
            il.append(toThrow);
            suppressed = il.append(new ASTORE(4));
            il.append(new ALOAD(handler == Handler.SWAPPED_SUPPRESSED ? 4 : 3));
            il.append(new ALOAD(handler == Handler.SWAPPED_SUPPRESSED ? 3 : 4));
            il.append(factory.createInvoke("java.lang.Throwable", "addSuppressed", Type.VOID, new Type[] { THROWABLE },
                    Const.INVOKEVIRTUAL));
            throwTarget = il.append(new ALOAD(handler == Handler.OTHER_THROWN ? 4 : 3));
            il.append(new ATHROW());
        }
        InstructionHandle ret = il.append(new RETURN());

        toEnd.setTarget(ret);
        if (handler != Handler.NO_INNER_HANDLER) {
            toThrow.setTarget(throwTarget);
        }
        if (skipHandlerClose != null) {
            skipHandlerClose.setTarget(throwTarget);
        }
        if (skipNormalClose != null) {
            skipNormalClose.setTarget(gotoEnd);
        }

        mg.addExceptionHandler(start, end, handlerStart, THROWABLE);
        if (suppressed != null) {
            mg.addExceptionHandler(closeStart, closeEnd, suppressed, THROWABLE);
        }
        mg.addLocalVariable("r", STREAM, 2, start, ret);
        mg.addLocalVariable("t", THROWABLE, 3, handlerStart.getNext() == null ? handlerStart : handlerStart.getNext(), ret);
        if (suppressed != null) {
            mg.addLocalVariable("s", THROWABLE, 4, suppressed.getNext(), throwTarget);
        }
        mg.setMaxStack();
        mg.setMaxLocals();
        cg.addMethod(mg.getMethod());
        cg.addEmptyConstructor(Const.ACC_PUBLIC);
        return cg.getJavaClass().getBytes();
    }
}
