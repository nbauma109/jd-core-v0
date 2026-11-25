package jd.core.test.fallbacks;

public interface WildcardGatherer<T, A, R> {
    default <S> WildcardGatherer<T, ?, S> andThen(WildcardGatherer<? super R, ?, ? extends S> that) {
        return WildcardComposite.of(this, that);
    }
}
