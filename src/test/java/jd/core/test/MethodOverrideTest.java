package jd.core.test;

import java.util.Map;

import org.apache.bcel.Const;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.InstructionConst;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.jd.core.v1.api.loader.Loader;
import org.junit.Test;

import jd.core.process.deserializer.ClassFileDeserializer;

import static org.junit.Assert.assertEquals;

public class MethodOverrideTest {
    @Test
    public void inferredOverridesRespectInheritedVisibilityAndModifiers() {
        int[][] cases = {
            { Const.ACC_PUBLIC, 0, 0 },
            { Const.ACC_PUBLIC, Const.ACC_PROTECTED, 0 },
            { Const.ACC_PUBLIC, Const.ACC_PUBLIC, 1 },
            { Const.ACC_PROTECTED, 0, 0 },
            { Const.ACC_PROTECTED, Const.ACC_PROTECTED, 1 },
            { Const.ACC_PROTECTED, Const.ACC_PUBLIC, 1 },
            { 0, 0, 1 },
            { 0, Const.ACC_PUBLIC, 1 },
            { Const.ACC_PRIVATE, Const.ACC_PUBLIC, 0 },
            { Const.ACC_STATIC, Const.ACC_PUBLIC, 0 },
            { Const.ACC_FINAL, Const.ACC_PUBLIC, 0 },
            { Const.ACC_PUBLIC, Const.ACC_PRIVATE, 0 },
            { Const.ACC_PUBLIC, Const.ACC_STATIC, 0 }
        };
        for (int[] test : cases) {
            Map<String, byte[]> classes = Map.of(
                    "test/Parent", type("test/Parent", "java/lang/Object", test[0]),
                    "test/Child", type("test/Child", "test/Parent", test[1]));
            Loader loader = new Loader() {
                @Override
                public boolean canLoad(String name) {
                    return classes.containsKey(name.replace(".class", ""));
                }

                @Override
                public byte[] load(String name) {
                    return classes.get(name.replace(".class", ""));
                }
            };
            var child = ClassFileDeserializer.deserialize(loader, "test/Child.class");
            assertEquals("Parent flags " + test[0] + ", child flags " + test[1], test[2] == 1,
                    child.getMethod("method", "()V").isOverride(child));
        }
    }

    private static byte[] type(String name, String parent, int flags) {
        ClassGen generated = new ClassGen(name.replace('/', '.'), parent.replace('/', '.'), "Override.java",
                Const.ACC_PUBLIC | Const.ACC_SUPER, new String[0]);
        MethodGen method = new MethodGen(flags, Type.VOID, Type.NO_ARGS, new String[0], "method",
                generated.getClassName(), new InstructionList(InstructionConst.RETURN), generated.getConstantPool());
        generated.addMethod(method.getMethod());
        return generated.getJavaClass().getBytes();
    }
}
