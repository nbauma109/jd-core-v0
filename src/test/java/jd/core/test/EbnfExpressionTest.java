package jd.core.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class EbnfExpressionTest extends AbstractTestCase {

    @Test
    public void test() throws Exception {
        String output = decompile("net/sourceforge/plantuml/ebnf/EbnfExpression");
        assertEquals(IOUtils.toString(getClass().getResource("EbnfExpression.txt"), StandardCharsets.UTF_8), output);
    }
}
