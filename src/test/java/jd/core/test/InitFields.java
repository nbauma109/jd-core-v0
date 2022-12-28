package jd.core.test;

import java.util.Random;

public class InitFields {
    int[] a = { 1, 2, 3 };
    int i = 0;
    Random r = new Random(System.currentTimeMillis());
    int j = r.nextBoolean() ? a[i] + a.length : -a[i];
    int k = ++i;
    int l = k++;
    Number[] nums = new Number[1];
    Number n1 = nums[0] = r.nextDouble();
    Number n2 = nums[0] = r.nextLong();
    Number n = r.nextBoolean() ? n1 : n2;
    boolean flag = n instanceof Double;
    Double d = flag ? (Double) n : new int[0][0].length;
}
