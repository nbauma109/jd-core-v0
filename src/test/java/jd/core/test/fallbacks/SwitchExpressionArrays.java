package jd.core.test.fallbacks;

/** Switch expressions giving the elements of an array initializer. */
@SuppressWarnings("java:S1488") // Initializing arrays with values before using them is the shape under test
public class SwitchExpressionArrays {
    static int failures;
    static StringBuilder log = new StringBuilder();

    static int work(int i) {
        if ((failures & i) != 0) {
            throw new IllegalStateException("w" + i);
        }
        log.append(i);
        return i;
    }

    static int array(int k) {
        int[] a = { switch (k) {
            case 0 -> 1;
            default -> 2;
        }, 7 };
        return a[0] * 10 + a[1];
    }

    static int array2(int k) {
        int[] a = new int[] { 7, switch (k) {
            case 0 -> 1;
            default -> 2;
        }, switch (k) {
            case 1 -> 3;
            default -> 4;
        } };
        return a[0] * 100 + a[1] * 10 + a[2];
    }

    static String strings(int k) {
        String[] a = { "x", switch (k) {
            case 0 -> "a";
            default -> "b";
        } };
        return a[0] + a[1];
    }

    static int varargs(int k) {
        return String.format("%s%s", switch (k) {
            case 0 -> "a";
            default -> "b";
        }, "c").charAt(0);
    }

    static int lastOfMany(int k) {
        int[] a = { work(1), work(2), switch (k) {
            case 0 -> work(4);
            default -> work(8);
        } };
        return a[0] * 100 + a[1] * 10 + a[2];
    }

    static int middle(int k) {
        int[] a = { work(1), switch (k) {
            case 0 -> work(4);
            default -> 3;
        }, work(2), 9 };
        return a[0] * 1000 + a[1] * 100 + a[2] * 10 + a[3];
    }

    static int zeros(int k) {
        int[] a = { 0, 5, switch (k) {
            case 0 -> 1;
            default -> 2;
        }, 0 };
        return a[0] * 1000 + a[1] * 100 + a[2] * 10 + a[3] + a.length * 10000;
    }

    static String format(int k) {
        return String.format("%s%s%s", "c", "d", switch (k) {
            case 0 -> "a";
            case 1 -> "e";
            default -> "b";
        });
    }

    static boolean flags(int k) {
        boolean[] a = { true, switch (k) {
            case 0 -> true;
            default -> false;
        } };
        return a[0] & a[1];
    }

    static String arrayBefore(int k) {
        return java.util.Arrays.toString(new int[] { 1, 2 }) + switch (k) {
            case 0 -> "a";
            default -> "b";
        };
    }

    static int plain(int k) {
        int[] a = { 1, k, k + 1 };
        return a[0] + a[1] + a[2];
    }

    static int ternary(int k) {
        int[] a = { 1, k > 0 ? 2 : 3, k };
        return a[0] + a[1] + a[2];
    }
}
