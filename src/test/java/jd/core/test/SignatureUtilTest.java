package jd.core.test;

import org.junit.Test;

import static org.junit.Assert.*;

import jd.core.util.SignatureUtil;

public class SignatureUtilTest {
    @Test
    public void testParameterSignatureCount() throws Exception {
        assertEquals(0, SignatureUtil.getParameterSignatureCount("()V"));
        assertEquals(1, SignatureUtil.getParameterSignatureCount("(I)V"));
        assertEquals(1, SignatureUtil.getParameterSignatureCount("(Ljava/util/Map<+TT;-TT;>;)V"));
        assertEquals(2, SignatureUtil.getParameterSignatureCount("(IJ)V"));
        assertEquals(1, SignatureUtil.getParameterSignatureCount("([[Ljava/lang/String;)V"));
        assertEquals(2, SignatureUtil.getParameterSignatureCount("(Ljava/lang/String;[I)V"));
    }
}
