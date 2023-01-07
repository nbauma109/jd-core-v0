package jd.core.test;

public class ArrayEquals {

    @SuppressWarnings("all")
    public static boolean equals(int[] a, int[] b) {
        if (a == null) {
           return b == null;
        } else if (b == null) {
           return false;
        } else {
           int i = a.length;
           if (i != b.length) {
              return false;
           } else {
              while(i-- > 0) {
                 if (a[i] != b[i]) {
                    return false;
                 }
              }
              return true;
           }
        }
     }
}
