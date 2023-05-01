package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.jd.core.v1.util.ZipLoader;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class WideInstructionTest extends AbstractTestCase {

    @Test
    public void test() throws Exception {
        String output = decompile("jd/core/test/WideInstruction");
        assertEquals(IOUtils.toString(getClass().getResource("WideInstruction.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void testECJInnerClasses() throws Exception {
        String output = decompile("jd/core/test/WideInstructionInnerClasses");
        assertEquals(IOUtils.toString(getClass().getResource("WideInstructionInnerClasses.txt"), StandardCharsets.UTF_8), output);
    }

    @Test
    public void testECJ17() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/wide-instructions-ecj17.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/WideInstruction", loader);
            assertEquals(IOUtils.toString(getClass().getResource("WideInstructionECJ17.txt"), StandardCharsets.UTF_8), output);
        }
    }
    
    @Test
    public void testECJ142() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/wide-instructions-ecj1.4.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/WideInstruction", loader);
            assertEquals(IOUtils.toString(getClass().getResource("WideInstructionECJ142.txt"), StandardCharsets.UTF_8), output);
        }
    }
    
    @Test
    public void testJDK1704() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/wide-instructions-jdk17.0.4.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/WideInstruction", loader);
            assertEquals(IOUtils.toString(getClass().getResource("WideInstruction1704.txt"), StandardCharsets.UTF_8), output);
        }
    }

    @Test
    public void testJDK180() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/wide-instructions-jdk1.8.0_331.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/WideInstruction", loader);
            assertEquals(IOUtils.toString(getClass().getResource("WideInstruction180.txt"), StandardCharsets.UTF_8), output);
        }
    }
    
    @Test
    public void testJDK142() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/wide-instructions-jdk1.4.2.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/WideInstruction", loader);
            assertEquals(IOUtils.toString(getClass().getResource("WideInstruction142.txt"), StandardCharsets.UTF_8), output);
        }
    }
    
    @Test
    public void testJDK122() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/wide-instructions-jdk1.2.2.jar")) {
            ZipLoader loader = new ZipLoader(in);
            String output = decompile("jd/core/test/WideInstruction", loader);
            assertEquals(IOUtils.toString(getClass().getResource("WideInstruction122.txt"), StandardCharsets.UTF_8), output);
        }
    }
    
    @Override
    protected boolean recompile() {
        return false;
    }
}
