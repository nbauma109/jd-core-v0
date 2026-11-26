package jd.core.test.fallbacks;

/** Cases of a switch ending with a try statement, which holds the jumps to the end of the switch. */
public class SwitchCaseEndingWithTry {
    static void single(int i) {
        switch (i) {
            case 0:
                try {
                    work(1);
                } catch (RuntimeException e) {
                    work(2);
                }
                break;
            default:
                work(4);
        }
        work(5);
    }

    static void emptyCatch(int i) {
        switch (i) {
            case 0:
                try {
                    work(1);
                } catch (RuntimeException e) {
                }
                break;
            default:
                work(4);
        }
        work(5);
    }

    static void middle(int i) {
        switch (i) {
            case 0:
                try {
                    work(1);
                } catch (RuntimeException e) {
                    work(2);
                }
                break;
            case 1:
                work(3);
                break;
            default:
                work(4);
        }
        work(5);
    }

    static void afterTry(int i) {
        switch (i) {
            case 0:
                try {
                    work(1);
                } catch (RuntimeException e) {
                    work(2);
                }
                work(3);
                break;
            default:
                work(4);
        }
        work(5);
    }

    static void fin(int i) {
        switch (i) {
            case 0:
                try {
                    work(1);
                } finally {
                    work(2);
                }
                break;
            default:
                work(4);
        }
        work(5);
    }

    static void lastCase(int i) {
        switch (i) {
            case 1:
                work(3);
                break;
            case 0:
                try {
                    work(1);
                } catch (RuntimeException e) {
                    work(2);
                }
                break;
        }
        work(5);
    }

    static void loopAround(int n) {
        for (int i = 0; i < n; i++) {
            switch (i) {
                case 0:
                    try {
                        work(1);
                    } catch (RuntimeException e) {
                        work(2);
                    }
                    break;
                default:
                    work(4);
            }
            work(5);
        }
    }

    static void nestedInCatch(int i) {
        switch (i) {
            case 0:
                try {
                    work(1);
                } catch (RuntimeException e) {
                    try {
                        work(2);
                    } catch (RuntimeException f) {
                        work(3);
                    }
                }
                break;
            default:
                work(4);
        }
        work(5);
    }

    static void throwingBody(int i) {
        switch (i) {
            case 0:
                try {
                    work(1);
                    throw new IllegalStateException();
                } catch (RuntimeException e) {
                    work(2);
                }
                break;
            default:
                work(4);
        }
        work(5);
    }

    static void conditionAroundTry(int[] values, int wanted) {
        for (int j = 0; j < values.length; j++) {
            if (values[j] == wanted) {
                if (wanted == 0) {
                    work(1);
                } else if (wanted > 1) {
                    try {
                        work(2);
                    } catch (RuntimeException e) {
                        work(3);
                    }
                } else if (wanted > 0) {
                    work(4);
                }
                break;
            }
            work(5);
        }
        work(6);
    }

    static int failures;

    static void work(int i) {
        System.out.println(i);
        if ((failures & 1 << i) != 0) {
            throw new IllegalStateException();
        }
    }
}
