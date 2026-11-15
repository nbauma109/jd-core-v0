package jd.core.test;

import java.util.function.Supplier;

public class ExplicitRuntimeCheckCast {

    interface Plain<T> {
        void put(T value);
    }

    /** The cast to String is a runtime check written in the source, not the erasure of the type variable. */
    @SuppressWarnings("unchecked")
    static <R> void checked(Plain<R> sink, Supplier<R> source) {
        sink.put((R) (String) source.get());
    }
}
