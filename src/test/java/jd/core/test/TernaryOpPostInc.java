package jd.core.test;

import java.awt.Point;

public class TernaryOpPostInc {

    Point p;

    void putfieldPostInc(boolean flag) {
        p.y = flag ? p.x++ : p.y--;
    }
}
