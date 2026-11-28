package jd.core.test.fallbacks;

/** Switch expressions giving the values of the cases of other switch expressions. */
// Nested switches and values stored before being returned are the shapes under test
@SuppressWarnings({ "java:S1488", "java:S1821" })
public class NestedSwitchExpressions {
    static int failures;
    static StringBuilder log = new StringBuilder();
    static boolean flag;

    static int work(int i) {
        if ((failures & i) != 0) {
            throw new IllegalStateException("w" + i);
        }
        log.append(i);
        return i;
    }

    static int first(int k, int j) {
        return switch (k) {
            case 0 -> switch (j) {
                case 0 -> 1;
                default -> 2;
            };
            case 1 -> 4;
            default -> 8;
        };
    }

    static int middle(int k, int j) {
        return switch (k) {
            case 0 -> 1;
            case 1 -> switch (j) {
                case 0 -> 2;
                case 1 -> work(1);
                default -> 4;
            };
            default -> 8;
        };
    }

    static int last(int k, int j) {
        int r = switch (k) {
            case 0 -> 1;
            default -> switch (j) {
                case 0 -> 2;
                default -> 4;
            };
        };
        return r;
    }

    static int both(int k, int j) {
        return switch (k) {
            case 0 -> switch (j) {
                case 0 -> 1;
                default -> 2;
            };
            default -> switch (j) {
                case 1 -> 3;
                default -> 4;
            };
        };
    }

    static int deep(int k, int j) {
        return switch (k) {
            case 0 -> switch (j) {
                case 0 -> switch (j + k) {
                    case 0 -> 1;
                    default -> 2;
                };
                default -> 3;
            };
            default -> 4;
        };
    }

    static int argument(int k, int j) {
        return work(switch (k) {
            case 0 -> switch (j) {
                case 0 -> 1;
                default -> 2;
            };
            default -> 4;
        });
    }

    static String strings(int k, int j) {
        String s = switch (k) {
            case 0 -> switch (j) {
                case 0 -> "a";
                default -> "b";
            };
            default -> "c";
        };
        return s;
    }

    static int statement(int k, int j) {
        int r = 0;
        switch (k) {
            case 0:
                switch (j) {
                    case 0:
                        r = 1;
                        break;
                    default:
                        r = 2;
                }
                break;
            default:
                r = 4;
        }
        return r;
    }

    static int inStatement(int k, int j) {
        int r = 0;
        switch (k) {
            case 0:
                r = switch (j) {
                    case 0 -> 1;
                    default -> 2;
                };
                break;
            default:
                r = 4;
        }
        return r;
    }

    static boolean bools(int k, int j) {
        boolean r = switch (k) {
            case 0 -> switch (j) {
                case 0 -> true;
                default -> false;
            };
            default -> true;
        };
        return r;
    }

    static String chars(int k, int j) {
        return String.valueOf(switch (k) {
            case 0 -> switch (j) {
                case 0 -> 'a';
                default -> 'b';
            };
            default -> 'c';
        });
    }

    static boolean stored(int k, int j) {
        flag = switch (k) {
            case 0 -> true;
            default -> switch (j) {
                case 0 -> true;
                default -> false;
            };
        };
        return flag;
    }

    static int conditions(int k, int j) {
        return switch (k) {
            case 0 -> switch (j) {
                case 0 -> j > k ? 1 : 2;
                default -> 3;
            };
            default -> j > 2 ? 4 : 5;
        };
    }

    static int throwing(int k, int j) {
        return switch (k) {
            case 0 -> switch (j) {
                case 0 -> 1;
                case 1 -> 2;
                default -> throw new IllegalArgumentException();
            };
            default -> 4;
        };
    }

    static int throwingOuter(int k, int j) {
        return switch (k) {
            case 0 -> switch (j) {
                case 0 -> 1;
                default -> 2;
            };
            case 1 -> 4;
            default -> throw new IllegalArgumentException();
        };
    }

    static int block(int k, int j) {
        return switch (k) {
            case 0 -> {
                work(1);
                yield switch (j) {
                    case 0 -> 1;
                    default -> 2;
                };
            }
            default -> 4;
        };
    }

    static int outerOperand(int k, int j) {
        return work(1) + switch (k) {
            case 0 -> switch (j) {
                case 0 -> 1;
                default -> 2;
            };
            default -> 8;
        };
    }

    static int sequence(int k, int j) {
        int a = switch (k) {
            case 0 -> switch (j) {
                case 0 -> 1;
                default -> 2;
            };
            default -> 8;
        };
        int b = switch (j) {
            case 1 -> 3;
            default -> switch (k) {
                case 1 -> 4;
                default -> 5;
            };
        };
        return a * 10 + b;
    }
}
