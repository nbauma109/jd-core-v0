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
}
