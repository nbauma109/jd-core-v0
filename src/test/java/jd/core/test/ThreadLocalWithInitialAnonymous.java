package jd.core.test;

public class ThreadLocalWithInitialAnonymous {

    static final ThreadLocal<Integer> TL = ThreadLocal.withInitial(() -> null);
}
