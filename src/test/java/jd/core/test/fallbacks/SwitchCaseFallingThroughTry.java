package jd.core.test.fallbacks;

/** A case ending with a try statement which falls through to the next case gets no "break". */
public class SwitchCaseFallingThroughTry {
    static int failures;

    @SuppressWarnings("java:S128") // Falling through is the shape under test
    static void afterFinally(int i) {
        switch (i) {
            case 0:
                try {
                    work(1);
                } finally {
                    work(2);
                }
            case 1:
                work(3);
                break;
            default:
                work(4);
        }
        work(5);
    }

    static void work(int i) {
        System.out.println(i);
        if ((failures & 1 << i) != 0) {
            throw new IllegalStateException();
        }
    }
}
