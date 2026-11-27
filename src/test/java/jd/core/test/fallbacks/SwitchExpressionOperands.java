package jd.core.test.fallbacks;

/** Switch expressions used other than to assign or to return their value. */
public class SwitchExpressionOperands {
    static int failures;
    static StringBuilder log = new StringBuilder();
    static int field;
    int instanceField;

    static int work(int i) {
        if ((failures & i) != 0) {
            throw new IllegalStateException("w" + i);
        }
        log.append(i);
        return i;
    }

    static int assigned(int k) {
        int r = switch (k) {
            case 0 -> work(1);
            case 1 -> work(2);
            default -> 7;
        };
        return r + 1;
    }

    static int returned(int k) {
        return switch (k) {
            case 0 -> work(1);
            case 1 -> work(2);
            default -> 7;
        };
    }

    static int argument(int k) {
        return work(switch (k) {
            case 0 -> 1;
            case 1 -> 2;
            default -> 4;
        });
    }

    static void statementArgument(int k) {
        work(switch (k) {
            case 0 -> 1;
            case 1 -> 2;
            default -> 4;
        });
    }

    static int secondArgument(int k) {
        return Math.max(work(8), switch (k) {
            case 0 -> 1;
            case 1 -> 2;
            default -> 4;
        });
    }

    static int operand(int k) {
        return work(1) + switch (k) {
            case 0 -> work(2);
            default -> 4;
        };
    }

    static String concat(int k) {
        return "<" + switch (k) {
            case 0 -> "a";
            case 1 -> "b";
            default -> "c";
        } + ">";
    }

    static int staticField(int k) {
        field = switch (k) {
            case 0 -> work(1);
            case 1 -> work(2);
            default -> 4;
        };
        return field;
    }

    static int compound(int k) {
        int r = work(8);
        r += switch (k) {
            case 0 -> work(1);
            case 1 -> work(2);
            default -> 4;
        };
        return r;
    }

    static int condition(int k) {
        if (work(8) < switch (k) {
            case 0 -> work(1);
            case 1 -> work(2);
            default -> 4;
        }) {
            return work(16);
        }
        return work(32);
    }

    static int throwingDefault(int k) {
        return work(switch (k) {
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 4;
            default -> throw new IllegalArgumentException();
        });
    }

    static int throwingMiddle(int k) {
        return work(switch (k) {
            case 0 -> 1;
            case 1 -> throw new IllegalArgumentException();
            default -> 4;
        });
    }

    static int twice(int k) {
        return Math.max(switch (k) {
            case 0 -> 1;
            case 1 -> 2;
            default -> 4;
        }, 0) * 10 + switch (k) {
            case 0 -> 1;
            case 1 -> 2;
            default -> 4;
        };
    }

    static int inStatement(int k) {
        switch (k) {
            case 0:
                work(switch (k) {
                    case 0 -> 1;
                    default -> 2;
                });
                break;
            default:
                work(4);
        }
        return work(8);
    }

    static int inLoop(int k) {
        int r = 0;
        for (int i = 0; i < 3; i++) {
            r = r * 10 + switch (k + i) {
                case 1 -> 1;
                case 2 -> 2;
                default -> 3;
            };
        }
        return r;
    }

    static int fieldOfInstance(int k) {
        SwitchExpressionOperands e = new SwitchExpressionOperands();
        e.instanceField += switch (k) {
            case 0 -> work(1);
            default -> 4;
        };
        field += switch (k) {
            case 0 -> work(2);
            default -> 4;
        };
        return e.instanceField * 10 + field;
    }

    static int minus(int k) {
        int r = work(16);
        r -= switch (k) {
            case 0 -> 1;
            default -> 2;
        };
        r *= switch (k) {
            case 0 -> work(1);
            default -> 3;
        };
        return r;
    }

    @SuppressWarnings("java:S1488") // Storing the value of the switch expression is the shape under test
    static boolean assignedBoolean(int k) {
        boolean r = switch (k) {
            case 0 -> true;
            case 1 -> throw new IllegalArgumentException();
            default -> false;
        };
        return r;
    }

    static void statementThenStatement(int k) {
        switch (k) {
            case 0:
                work(1);
                break;
            default:
                work(2);
        }
        work(4);
    }
}
