package jd.core.test;

public class ThreadLocalWithInitialLambda {

    static final ThreadLocal<Integer> TL = ThreadLocal.withInitial(() -> null);
}
