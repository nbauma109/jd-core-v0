package jd.core.test;

public class Dup2Pop2 {

    long l1;
    long l2;
    double[] darr;

    void dup2x1Form1(String[] s) {
        s[0] += "s";
    }

    void dup2x1Form2() {
        l2 = l1 = 1;
    }

    long dup2Form1(long a) {
        a++;
		return a;
    }

    void dup2Form2(double d) {
        darr[0] += d;
    }

    long dup2x2(long[] array, int i, long l) {
        return array[i] = l;
    }

    void pop2() {
        Math.round(0.5d);
    }
}
