package jd.core.test.fallbacks;

/** Cases of a switch falling through a try statement, returning from it, or ending with other blocks. */
public class SwitchCaseShapes {
    static int failures;

    @SuppressWarnings("java:S128") // Falling through is the shape under test
    static void finallyFallsThrough(int i) {
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

    @SuppressWarnings("java:S128") // Falling through is the shape under test
    static void catchFallsThrough(int i) {
        switch (i) {
            case 0:
                try {
                    work(1);
                } catch (RuntimeException e) {
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

    @SuppressWarnings("java:S128") // The missing "break" is the shape under test
    static void returningFinally(int i) {
        switch (i) {
            case 0:
                try {
                    work(1);
                    return;
                } finally {
                    work(2);
                }
            default:
                work(4);
        }
        work(5);
    }

    @SuppressWarnings("java:S2737") // The rethrowing "catch" block is the shape under test
    static void returningCatchRethrows(int i) {
        switch (i) {
            case 0:
                try {
                    work(1);
                    return;
                } catch (RuntimeException e) {
                    throw e;
                }
            default:
                work(4);
        }
        work(5);
    }

    @SuppressWarnings({"java:S2737", "java:S128"}) // The rethrowing "catch" block and the missing "break" are under test
    static void returningCatchFinally(int i) {
        switch (i) {
            case 0:
                try {
                    work(1);
                    return;
                } catch (RuntimeException e) {
                    throw e;
                } finally {
                    work(6);
                }
            default:
                work(4);
        }
        work(5);
    }

    static void monitor(int i) {
        switch (i) {
            case 0:
                synchronized (SwitchCaseShapes.class) {
                    work(1);
                }
                break;
            default:
                work(4);
        }
        work(5);
    }

    static void catchFinally(int i) {
        switch (i) {
            case 0:
                try {
                    work(1);
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
