package jd.core.test;

public class ForWithIndexes {

    public static void test(int n) {
        for (int i = 0, j = 0, k = 0; (i < n) && (j < i) && (k < j); i++, j++, k++) {
            System.out.println(i);
            System.out.println(j);
            System.out.println(k);
        }
        for (long i = 0L, j = 0L, k = 0L; (i < n) && (j < i) && (k < j); i++, j++, k++) {
            System.out.println(i);
            System.out.println(j);
            System.out.println(k);
        }
        for (double i = 0.0D, j = 0.0D, k = 0.0D; (i < n) && (j < i) && (k < j); i++, j++, k++) {
            System.out.println(i);
            System.out.println(j);
            System.out.println(k);
        }
        for (float i = 0.0F, j = 0.0F, k = 0.0F; (i < n) && (j < i) && (k < j); i++, j++, k++) {
            System.out.println(i);
            System.out.println(j);
            System.out.println(k);
        }
    }
}
