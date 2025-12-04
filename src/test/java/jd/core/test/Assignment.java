package jd.core.test;

import java.awt.*;

public class Assignment {

    static int i, j, k;
    int a, b, c;

    void add(int x) {
        i = i + x;
    }

    void add(byte x) {
        i =
        i + x;
    }

    void add(short x) {
        i = j + x;
    }

    void add(int x, int y) {
        i = y + x;
    }

    static void add(Point p, int x) {
        p.x += x;
    }

    int mul(int x) {
        i *= 1;
        a *= 1;
        x *= 1;
        i *= -1;
        a *= -1;
        x *= -1;
        return x;
    }

    int increment(int x) {
        i++;
        a++;
        x++;
        i += 1;
        a += 1;
        x += 1;
        i -= -1;
        a -= -1;
        x -= -1;
        i += 2;
        a += 2;
        x += 2;
        return x;
    }

    int decrement(int x) {
        i--;
        a--;
        x--;
        i -= 1;
        a -= 1;
        x -= 1;
        i += -1;
        a += -1;
        x += -1;
        i -= 2;
        a -= 2;
        x -= 2;
        return x;
    }

    void multiAssign() {
        b = c;
        a = b;
    }

    void multiAssignIncPutField() {
        a = b += c;
    }

    void multiAssignIncPutStatic() {
        i = j += k;
    }

    void multiAssignIncIStore(int a, int b, int c) {
        a = b += c;
    }

    void multiAssignIncStore(float a, float b, float c) {
        a = b += c;
    }

    void compute(Rectangle[] r) {
        r[7].y = (r[8].y = r[6].y = r[4].y + r[4].height);
    }
}
