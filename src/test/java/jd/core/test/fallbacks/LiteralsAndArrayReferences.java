package jd.core.test.fallbacks;

import java.util.function.IntFunction;

public class LiteralsAndArrayReferences {
    static String concatenation(String suffix) {
        return "descriptor [L[" + suffix;
    }

    static IntFunction<String[]> returned() {
        return String[]::new;
    }

    @SuppressWarnings("java:S1172") // The parameter gives the method reference its target type
    static Object argument(IntFunction<int[][]> generator) {
        return argument(int[][]::new);
    }
}
