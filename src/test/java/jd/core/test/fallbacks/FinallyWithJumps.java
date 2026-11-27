package jd.core.test.fallbacks;

/** "finally" blocks left by "continue" and "break". */
public class FinallyWithJumps {
    static int failures;
    static StringBuilder log = new StringBuilder();

    static void work(int i) {
        if ((failures & i) != 0) {
            throw new IllegalStateException("w" + i);
        }
        log.append(i);
    }

    static void both(int n) {
        for (int i = 0; i < n; i++) {
            try {
                if (i == 1) {
                    continue;
                }
                if (i == 3) {
                    break;
                }
                work(1);
            } finally {
                work(4);
            }
        }
        work(8);
    }

    static void onlyContinue(int n) {
        for (int i = 0; i < n; i++) {
            try {
                if (i == 1) {
                    continue;
                }
                work(1);
            } finally {
                work(4);
            }
        }
        work(8);
    }

    static void onlyBreak(int n) {
        for (int i = 0; i < n; i++) {
            try {
                if (i == 3) {
                    break;
                }
                work(1);
            } finally {
                work(4);
            }
        }
        work(8);
    }

    static void breakWhile(int n) {
        int i = 0;
        while (i < n) {
            try {
                i++;
                if (i == 2) {
                    break;
                }
                work(1);
            } finally {
                work(4);
            }
            work(2);
        }
        work(8);
    }
}
