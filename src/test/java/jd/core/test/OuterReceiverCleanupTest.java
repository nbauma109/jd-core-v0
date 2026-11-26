package jd.core.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.tools.ToolProvider;

import org.jd.core.v1.api.loader.Loader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OuterReceiverCleanupTest extends AbstractTestCase {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void restoresStableOuterReceiverWithoutRepeatingEffectfulReceiver() throws Exception {
        Path directory = temporaryFolder.getRoot().toPath();
        Path sourceFile = directory.resolve("SweepUpFixture.java");
        try (var source = getClass().getResourceAsStream("/sweepup/SweepUpFixture.java")) {
            Files.copy(source, sourceFile);
        }
        assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, null,
                "--release", "8", "-g", "-proc:none", "-Xlint:-options", "-d", directory.toString(), sourceFile.toString()));

        Loader loader = new Loader() {
            @Override
            public boolean canLoad(String internalName) {
                return Files.exists(directory.resolve(internalName + ".class"));
            }

            @Override
            public byte[] load(String internalName) throws IOException {
                return Files.readAllBytes(directory.resolve(internalName + ".class"));
            }
        };
        String source = decompile("sweepup/SweepUpFixture", loader);
        assertTrue(source, source.contains("combine(SweepUpFixture.this, SweepUpFixture.this.value())"));
        // One declaration and one call to next; duplicating the receiver would add another call.
        assertEquals(source, 3, source.split("next\\(\\)", -1).length);
    }
}
