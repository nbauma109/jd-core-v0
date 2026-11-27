package jd.core.test.fallbacks;

/** Switch expressions whose cases hold conditions. */
// Redundant literals, values stored before being returned and blocks yielding a value are the shapes under test
@SuppressWarnings({ "java:S1125", "java:S1488", "java:S3358", "java:S9358" })
public class SwitchExpressionConditions {
    static int failures;
    static StringBuilder log = new StringBuilder();

    static int work(int i) {
        if ((failures & i) != 0) {
            throw new IllegalStateException("w" + i);
        }
        log.append(i);
        return i;
    }

    static int compare(int k) {
        return work(switch (k) {
            case 0 -> k > 2 ? 1 : 2;
            case 1 -> 4;
            default -> 8;
        });
    }

    static boolean boolCase(int k) {
        boolean r = switch (k) {
            case 0 -> k > 2;
            case 1 -> true;
            default -> false;
        };
        return r;
    }

    static boolean andCase(int k, boolean b) {
        return switch (k) {
            case 0 -> b && work(1) > 0;
            case 1 -> b || k > 2;
            default -> !b;
        };
    }

    static int ternaryAssign(int k, boolean b) {
        int r = switch (k) {
            case 0 -> b ? work(1) : work(2);
            default -> 4;
        };
        return r;
    }

    static int chain(int k, boolean b) {
        return switch (k) {
            case 0 -> b ? work(1) : k > 2 ? work(2) : work(4);
            case 1 -> k > 0 ? (b ? work(8) : work(16)) : work(32);
            default -> 3;
        };
    }

    static String strings(int k, boolean b) {
        String s = switch (k) {
            case 0 -> b ? "a" : "b";
            case 1 -> "c";
            default -> b ? "d" : "e";
        };
        return s;
    }

    static int lastCase(int k, boolean b) {
        return switch (k) {
            default -> 3;
            case 0 -> 4;
            case 1 -> b ? 1 : 2;
        };
    }

    static int arithmetic(int k, boolean b) {
        int r = switch (k) {
            case 0 -> (b ? work(1) : work(2)) + 10;
            case 1 -> work(4) * (k > 0 ? 3 : 5);
            default -> 3;
        };
        return r;
    }

    static int operand(int k, boolean b) {
        return work(1) + switch (k) {
            case 0 -> b ? work(2) : work(4);
            default -> 8;
        };
    }

    static int argUnder(int k, boolean b) {
        return Math.max(work(1), switch (k) {
            case 0 -> b ? 2 : 4;
            default -> 8;
        });
    }

    static int after(int k, boolean b) {
        return work(switch (k) {
            case 0 -> 1;
            default -> 2;
        }) + (b ? 4 : 8);
    }

    static int statement(int k, boolean b) {
        int r = 0;
        switch (k) {
            case 0:
                r = b ? 1 : 2;
                break;
            case 1:
                r = work(b ? 4 : 8);
                break;
            default:
                r = k > 3 ? 16 : 32;
        }
        return r;
    }

    static int statementThen(int k, boolean b) {
        switch (k) {
            case 0:
                work(1);
                break;
            default:
                work(2);
        }
        return b ? 4 : 8;
    }

    static int statementArgs(int k, boolean b) {
        switch (k) {
            case 0:
                work(b ? 1 : 2);
                break;
            default:
                work(k > 2 ? 4 : 8);
        }
        return 1;
    }

    static int blocks(int k, boolean b) {
        return switch (k) {
            case 0 -> {
                work(1);
                yield b ? 2 : 4;
            }
            default -> 8;
        };
    }

    static int caseThrows(int k, boolean b) {
        return switch (k) {
            case 0 -> b ? 1 : 2;
            case 1 -> throw new IllegalStateException();
            default -> b ? 3 : 4;
        };
    }

    static boolean negated(int k, boolean b) {
        return switch (k) {
            case 0 -> k > 2 || b;
            case 1 -> !(k > 0 && b);
            default -> k == 3;
        };
    }
}
