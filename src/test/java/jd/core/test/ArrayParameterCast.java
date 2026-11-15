package jd.core.test;

import java.util.function.Supplier;

public class ArrayParameterCast {

    interface Sink<T> {
        void set(T[] value);
    }

    /** The cast of the argument is not redundant: the parameter is an array of the type variable. */
    @SuppressWarnings("unchecked")
    static <R> void copy(Sink<R> sink, Supplier<R> supplier) {
        sink.set((R[]) supplier.get());
    }
}
