package jd.core.test.fallbacks;

/** Ternary operators nested in one another. */
// Nested operators, redundant literals and values stored before being returned are the shapes under test
@SuppressWarnings({ "java:S3358", "java:S1125", "java:S1488" })
public class NestedTernaries {
    static int failures;
    static StringBuilder log = new StringBuilder();

    static int work(int i) {
        if ((failures & i) != 0) {
            throw new IllegalStateException("w" + i);
        }
        log.append(i);
        return i;
    }

    static String nested(int k) {
        String s = k == 0 ? "a" : k == 1 ? "b" : "zz";
        return s;
    }

    static String nested3(int k) {
        String s = k == 0 ? "a" : k == 1 ? "b" : k == 2 ? "c" : "d";
        return s + s;
    }

    static String nestedLeft(int k, boolean b) {
        String s = k == 0 ? (b ? "a" : "b") : "c";
        return s;
    }

    static int nestedInt(int k) {
        int s = k == 0 ? work(1) : k == 1 ? work(2) : work(4);
        return s;
    }

    static int nested5(int k) {
        int s = k == 0 ? work(1) : k == 1 ? work(2) : k == 2 ? work(4) : k == 3 ? work(8) : work(16);
        return s;
    }

    static int returned(int k) {
        return k == 0 ? work(1) : k == 1 ? work(2) : work(4);
    }

    static int argument(int k) {
        return work(k == 0 ? work(1) : k == 1 ? work(2) : work(4));
    }

    static int both(int k, boolean b) {
        int r = k > 1 ? (b ? work(1) : work(2)) : (k == 1 ? work(4) : work(8));
        return r;
    }

    static int deepLeft(int k, boolean b) {
        int r = k > 1 ? (b ? (k > 2 ? work(1) : work(2)) : work(4)) : work(8);
        return r;
    }

    static int mixed(int k, boolean b) {
        int r = k > 1 ? (b ? work(1) : k > 2 ? work(2) : work(4)) : k == 0 ? work(8) : b ? work(16) : work(32);
        return r;
    }

    static int complex(int k, boolean b) {
        int r = k == 0 && b ? work(1) : k == 1 || b ? work(2) : k > 2 && !b ? work(4) : work(8);
        return r;
    }

    static int constants(int k) {
        int r = k == 0 ? 10 : k == 1 ? 20 : k == 2 ? 30 : 40;
        return r;
    }

    static char chars(int k) {
        char r = k == 0 ? 'a' : k == 1 ? 'b' : 'c';
        return r;
    }

    static boolean bools(int k, boolean b) {
        boolean r = k == 0 ? b : k == 1 ? !b : k > 2;
        return r;
    }

    static boolean boolConst(int k, boolean b) {
        boolean r = k == 0 ? true : k == 1 ? false : b;
        return r;
    }

    static String arg(int k) {
        return String.valueOf(k == 0 ? "a" : k == 1 ? "b" : "zz");
    }

    static String concat(int k) {
        return "<" + (k == 0 ? "a" : k == 1 ? "b" : "c") + ">";
    }

    static int twice(int k) {
        return (k == 0 ? 1 : k == 1 ? 2 : 3) * 10 + (k == 2 ? 4 : k == 3 ? 5 : 6);
    }

    static int inner(int k, boolean b) {
        int r = k == 0 ? work(1) : b ? (k > 2 ? work(2) : work(4)) : work(8);
        return r;
    }

    static int inner2(int k, boolean b) {
        int r = k == 0 ? work(1) : k == 1 ? work(2) : b ? (k > 2 ? (k > 3 ? work(4) : work(8)) : work(16)) : k == 2 ? work(32) : work(1);
        return r;
    }

    static boolean boolMix(int k, boolean b) {
        boolean r = k == 0 ? b : k == 1 ? true : false;
        return r;
    }

    static boolean boolMix2(int k, boolean b) {
        boolean r = k == 0 ? false : b;
        return r;
    }

    static boolean boolMix3(int k, boolean b) {
        return k == 0 ? true : k > 2 && b;
    }
}
