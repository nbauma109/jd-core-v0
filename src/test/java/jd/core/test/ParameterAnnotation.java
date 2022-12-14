package jd.core.test;
/*
 * Test input built from Procyon's wiki page:
 * https://github.com/mstrobel/procyon/wiki/Decompiler-Output-Comparison.
 */
public abstract class ParameterAnnotation {
    abstract void test(@TestAnnotation int i, @TestAnnotation(nested = @NestedAnnotation(123.45f)) int j);
}
