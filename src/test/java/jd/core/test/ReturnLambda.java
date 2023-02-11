package jd.core.test;

import java.util.function.Predicate;

public class ReturnLambda {

    Predicate<?> predicate;
    static Predicate<?> predicateStatic;

    Predicate<?> isNotNullReturn() {
        return a -> a != null;
    }

    Predicate<?> isNotNullStoreReturn() {
        Predicate<?> predicate = a -> a != null;
        return predicate;
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
