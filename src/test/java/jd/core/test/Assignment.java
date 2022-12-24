package jd.core.test;

import java.awt.Point;

public class Assignment {

    static int i, j;
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

    static void add (Point p, int x) {
        p.x += x;
    }

    // Assignation multiple sur deux lignes :
    // b = c;
    // a = b;
    void multiAssign() {
        b = c;
        a = b;
    }

    void multiAssignInc() {
        a = b += c;
    }

    void compute(Rectangle[] r) {
        r[7].y = (r[8].y = r[6].y = r[4].y + r[4].height);
    }
}
