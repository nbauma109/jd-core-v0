package jd.core.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/*
 * Simplified from commons-lang3 ArrayCollector.
 * TODO Handle decompilation failures of the real one.
 */
public class ArrayCollector<O> implements Collector<O, List<O>, O[]> {
    final Class<O> elementType;

    public ArrayCollector(final Class<O> elementType) {
        this.elementType = elementType;
    }

    @Override
    public BiConsumer<List<O>, O> accumulator() {
        return List::add;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }

    @Override
    public BinaryOperator<List<O>> combiner() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Function<List<O>, O[]> finisher() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Supplier<List<O>> supplier() {
        return ArrayList::new;
    }
}