package jd.core.test;

import java.awt.Point;

public class TernaryOpPreInc {

    Point p;

    void putfieldPreInc(boolean flag) {
        p.y = flag ? ++p.x : --p.y;
    }
}
