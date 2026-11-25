package jd.core.test.fallbacks;

/** Declares more type parameters than the interface it is returned as. */
public final class WildcardComposite<T, A, R, B, S> implements WildcardGatherer<T, Object, S> {
    @SuppressWarnings("java:S1172") // Only the generic signature matters
    static <T, A, R, B, S> WildcardComposite<T, ?, ?, ?, S> of(WildcardGatherer<T, A, ? extends R> left,
            WildcardGatherer<? super R, B, ? extends S> right) {
        return null;
    }
}
