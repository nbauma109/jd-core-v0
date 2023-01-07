package jd.core.test;

public class ArrayEquals {

    public static boolean equals(int[] a, int[] b) {
        if (a == null) {
            return b == null;
        }
        if (b == null) {
            return false;
        }
        int i = a.length;
        if (i != b.length) {
            return false;
        }
        while (i-- > 0) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
}
