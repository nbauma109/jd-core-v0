package jd.core.test.fallbacks;

import java.util.List;

/** ECJ writes no local variable table entry for the arguments captured by a lambda. */
public class CapturedLambdaArguments {
    static void inStaticMethod(List<String> list, String prefix) {
        list.removeIf(s -> {
            String text = prefix + s;
            return text.isEmpty();
        });
    }

    void withoutAnyEntry(Object[] values, String name) {
        Runnable r = () -> values[0] = new StringBuilder(name);
        r.run();
    }

    static Runnable withWideArgument(int[] counter, long step) {
        return () -> {
            long next = counter[0] + step;
            counter[0] = (int) next;
        };
    }
}
