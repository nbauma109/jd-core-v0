package jd.core.test.fallbacks;

/**
 * Cases which only the byte code of ECJ lets reconstruct so far: falling through a try statement, which gets no
 * "break", or returning from it while its "catch" block leaves the switch.
 */
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

    static void returningCatchBreaks(int i) {
        switch (i) {
            case 0:
                try {
                    work(1);
                    return;
                } catch (RuntimeException e) {
                    work(2);
                } finally {
                    work(6);
                }
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
