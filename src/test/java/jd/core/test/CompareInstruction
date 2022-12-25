package jd.core.test;

import java.awt.Point;
import java.util.Map;

public class CompareInstruction {

    int[] a;
    int x, y;
    Double d;
    Object o;

    public CompareInstruction(int[] a, Point p, Double d, Object o) {
        this.a = a;
        x = p.x;
        y = p.y;
        this.d = d;
        this.o = o;
    }

    void assertion(boolean flag, String message) {
        assert flag : message;
    }

    void athrow(String message) {
        throw new Exception(message);
    }

    int[] initArray() {
        return new int[] { x, y };
    }

    int arrayLength() {
        return a.length;
    }

    void arrayStore() {
        a[x] = y;
    }
 
    void arrayLoad(int x) {
        return a[x];
    }

    int unaryOp() {
        return -x;
    }

    int binaryOp() {
        return x + y;
    }

    byte convert() {
        return (byte) x;
    }

    void instanceOf() throws Exception {
        if (d instanceof Number) {
            System.out.println("ok");
        }
    }

    void fastSynchronized() {
        synchronized (o) {
            System.out.println("ok");
        }
    }

    Object[] aNewArray() {
        return new Object[x];
    }

    int[] newArray() {
        return new int[x];
    }

    Object[][] multiANewArray() {
        return new Object[x][y];
    }
}
