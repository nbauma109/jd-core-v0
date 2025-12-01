package jd.core.test;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class ReturnLambda {

    Predicate<?> predicate;
    static Predicate<?> predicateStatic;

    Predicate<?> isNotNullReturn() {
        return a -> a != null;
    }

    ToIntFunction<?> isNotNullReturnInt() {
        return a -> a != null ? 1 : 0;
    }

    Predicate<?> isNotNullStoreReturn() {
        return a -> a != null;
    }

    Predicate<?> isNotNullPutFieldReturn() {
        this.predicate = a -> a != null;
        return this.predicate;
    }

    static Predicate<?> isNotNullPutStaticReturn() {
        predicateStatic = a -> a != null;
        return predicateStatic;
    }
}
