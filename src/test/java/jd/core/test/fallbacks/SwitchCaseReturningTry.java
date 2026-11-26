package jd.core.test.fallbacks;

/** Cases of a switch whose try statement returns, leaving the "break" to its catch blocks. */
public class SwitchCaseReturningTry {
    static int failures;

    static void returnFinallyBreak(int i) {
        switch (i) {
            case 0:
                try {
                    work(1);
                    return;
                } catch (IllegalStateException e) {
                    work(2);
                } finally {
                    work(3);
                }
                break;
            default:
                work(4);
        }
        work(5);
    }

    static void returnFinallyConditionInCatch(int i, boolean b) {
        switch (i) {
            case 0:
                try {
                    work(1);
                    return;
                } catch (IllegalStateException e) {
                    if (b) {
                        work(2);
                    }
                } finally {
                    work(3);
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
