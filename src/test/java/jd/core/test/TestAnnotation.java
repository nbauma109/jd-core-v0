package jd.core.test;

import java.nio.file.StandardCopyOption;

/*
 * Test input built from Procyon's wiki page:
 * https://github.com/mstrobel/procyon/wiki/Decompiler-Output-Comparison.
 * TODO FIXME No need to import java.lang.annotation.Annotation.
 */
@interface TestAnnotation {
    String value() default "";

    String name() default "";

    Class<?> clazz() default Integer.class;

    Class<?>[] classes() default { Integer.class, Long.class };
    
    StandardCopyOption option() default StandardCopyOption.COPY_ATTRIBUTES;
    
    char[] characters() default { 'a', 'b' };

    byte[] bytes() default {};

    int integer() default 0;

    double real() default 0d;

    NestedAnnotation nested() default @NestedAnnotation(0f);
}
