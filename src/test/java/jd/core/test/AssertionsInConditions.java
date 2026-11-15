package jd.core.test;

/** Assertions inside conditions: the conditions that skip the assertion keep their operator. */
public class AssertionsInConditions {

    static void and(boolean first, int second, int value) {
        if (first && second != 0) {
            assert value > 0 : "value";
        }
    }

    static void andThree(boolean first, boolean second, boolean third, int value) {
        if (first && second && third) {
            assert value > 0;
        }
    }

    static void mixed(boolean a, boolean b, boolean c, int value) {
        if (a || b && c) {
            assert value > 0;
        }
    }

    static void or(boolean a, boolean b, int value) {
        if (a || b) {
            assert value > 0 : "v";
        }
    }

    static void unconditional(boolean first, int second) {
        if (first && second != 0) {
            assert false : "unreachable";
        }
    }
}
