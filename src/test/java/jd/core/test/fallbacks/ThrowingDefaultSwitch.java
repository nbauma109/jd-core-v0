package jd.core.test.fallbacks;

/** The value of the last case is only consumed if the default case, written after it, does not complete. */
public class ThrowingDefaultSwitch {
    static int assigned(int k, boolean trace) {
        if (trace) {
            System.out.println(k);
        }
        int result = switch (k) {
            case 0 -> 10;
            case 1 -> 20;
            default -> throw new IllegalStateException();
        };
        return result;
    }

    static String returned(int k, boolean trace) {
        if (trace) {
            System.out.println(k);
        }
        return switch (k) {
            case 0 -> "zero";
            case 1 -> "one";
            default -> throw new IllegalArgumentException();
        };
    }
}
