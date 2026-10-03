package jd.core.test;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MultilineExpressionTest extends AbstractTestCase {
    @Test
    public void preservesExpressionLines() throws Exception {
        String output = decompile("jd/core/test/MultilineExpression");
        assertTrue(output, output.contains("/* 10 */     return new Builder()\n")
                && output.contains("/* 11 */       .append(first)\n")
                && output.contains("/* 12 */       .append(second)\n")
                && output.contains("/* 13 */       .finish();"));
        assertTrue(output, output.contains("/* 17 */     return Math.max(\n")
                && output.contains("/* 18 */       Integer.parseInt(\"1\"), ")
                && output.contains("/* 19 */       Integer.parseInt(\"2\"));"));
    }
}
