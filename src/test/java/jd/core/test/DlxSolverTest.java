package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class DlxSolverTest extends AbstractTestCase {
    @Test
    public void test() throws Exception {
        String output = decompile("net/sourceforge/plantuml/sudoku/dlx_solver");
        assertEquals(IOUtils.toString(getClass().getResource("dlx_solver.txt"), StandardCharsets.UTF_8), output);
    }
}
