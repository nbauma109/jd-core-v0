package jd.core.test;

public class Declare {

    int istore(boolean flag, int inc) {
        int i;
        if (flag) {
            i = 0;
        } else {
            i = 1;
        }
        i += inc;
        return i;
    }

    float store(boolean flag, float inc) {
        float f;
        if (flag) {
            f = 0;
        } else {
            f = 1;
        }
        f += inc;
        return f;
    }

    Integer astore(boolean flag, int inc) {
        Integer i;
        if (flag) {
            i = 0;
        } else {
            i = 1;
        }
        i += inc;
        return i;
    }

    double assignment() {
        double d1;
        double d2;
        while ((d1 = Math.random()) < 0.5 && (d2 = Math.random()) < 0.5) {
            d1 = 0;
            d2 = 0;
        }
        d1 = 1;
        d2 = 2;
        return d1 + d2;
    }

    double storeAndAssignment() {
        double d1;
        double d2;
        double d3;
        double d4;
        do {
            d3 = 0;
            d4 = 0;
        } while ((d1 = Math.random()) < 0.5 || (d2 = Math.random()) < 0.5);
        if (Math.random() < 0.5) {
            d1 = 1;
            d2 = 2;
            d3 = 3;
            d4 = 4;
        }
        return d1 + d2 + d3 + d4;
    }
}
