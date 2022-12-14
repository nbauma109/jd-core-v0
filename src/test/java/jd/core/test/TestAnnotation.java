package jd.core.test;

/*
 * Test input built from Procyon's wiki page:
 * https://github.com/mstrobel/procyon/wiki/Decompiler-Output-Comparison.
 * TODO FIXME No need to import java.lang.annotation.Annotation.
 */
@interface TestAnnotation {
    String value() default "";

    String name() default "";

    char[] characters() default {};

    int integer() default 0;

    double real() default 0d;

    NestedAnnotation nested() default @NestedAnnotation(0f);
}
