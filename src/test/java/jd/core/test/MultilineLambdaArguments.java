package jd.core.test;

import java.util.function.IntPredicate;
import java.util.function.IntSupplier;

@SuppressWarnings("java:S1488") public class MultilineLambdaArguments {
    static int consume(int value, IntPredicate test, IntSupplier supplier) {
        return test.test(value) ? supplier.getAsInt() : 0;
    }

    static int call(int size) {
        int result = consume(size,
                value -> value > 0,
                () -> size);
        return result;
    }

    static int after() {
        return 42;
    }

    static int pipeline(java.util.List<Integer> values) {
        return values.stream()
                .map(value -> value)
                .reduce((first, second) -> first + second)
                .orElse(0);
    }

    static int assignedPipeline(java.util.List<Integer> values) {
        Integer result = values.stream()
                .map(value -> value)
                .reduce((first, second) -> first + second)
                .orElse(0);
        return result;
    }

    static final java.util.Comparator<String> COMPARATOR = (first, second) -> {
        int firstLength = first.length();
        int secondLength = second.length();
        return firstLength - secondLength;
    };
}
