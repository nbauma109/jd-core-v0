package jd.core.test;

import java.util.function.Supplier;

public class ThreadLocalWithInitialAnonymous {

    static final ThreadLocal<Integer> TL = ThreadLocal.withInitial(new Supplier<>() {

        @Override
        public Integer get() {
            return null;
        }
    });
}
