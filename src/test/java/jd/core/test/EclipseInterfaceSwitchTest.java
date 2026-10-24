package jd.core.test;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.tools.ToolProvider;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EclipseInterfaceSwitchTest extends AbstractTestCase {
    @Test
    public void interfaceEnumSwitchHasNoSyntheticTableReferences() throws Exception {
        String source = decompile("jd/core/test/syntheticcases/InterfaceSwitch");
        assertFalse(source, source.contains("$SWITCH_TABLE$"));
        assertFalse(source, source.contains("$SwitchMap$"));
        if ("ecj".equals(System.getProperty("test.compiler"))) {
            assertTrue(source, source.contains("case FIRST:"));
            assertTrue(source, source.contains("case SECOND:"));
        }
        // Newer javac versions switch directly on ordinal() rather than a synthetic map.
        assertRecompiledBehavior(source);
    }

    private static void assertRecompiledBehavior(String source) throws Exception {
        Path directory = Files.createTempDirectory(Path.of("target"), "interface-switch-");
        Path file = directory.resolve("InterfaceSwitch.java");
        Files.writeString(file, source, StandardCharsets.UTF_8);
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        int result = ToolProvider.getSystemJavaCompiler().run(null, null, errors,
                "--release", "17", "-proc:none", "-d", directory.toString(), file.toString());
        assertEquals(source + "\n" + errors.toString(StandardCharsets.UTF_8), 0, result);
        try (var loader = new URLClassLoader(new java.net.URL[] { directory.toUri().toURL() },
                ClassLoader.getPlatformClassLoader())) {
            Class<?> type = loader.loadClass("jd.core.test.syntheticcases.InterfaceSwitch");
            Class<?> kind = loader.loadClass("jd.core.test.syntheticcases.InterfaceSwitch$Kind");
            Object instance = Proxy.newProxyInstance(loader, new Class<?>[] { type },
                    (proxy, method, args) -> InvocationHandler.invokeDefault(proxy, method, args));
            var choose = type.getMethod("choose", kind);
            Object[] constants = kind.getEnumConstants();
            assertEquals(17, choose.invoke(instance, constants[0]));
            assertEquals(23, choose.invoke(instance, constants[1]));
            InvocationTargetException failure = assertThrows(InvocationTargetException.class,
                    () -> choose.invoke(instance, new Object[] { null }));
            assertTrue(failure.getCause() instanceof NullPointerException);
        }
    }
}
