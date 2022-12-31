package jd.core.test;

import java.awt.Point;

public class CompareInstruction {

    int[] a;
    int x, y;
    Number n;
    Object o;

    public CompareInstruction(int[] a, Point p, Number n, Object o) {
        this.a = a;
        x = p.x;
        y = p.y;
        this.n = n;
        this.o = o;
    }

    void assertion(boolean flag, String message) {
        assert flag : message;
    }

    void athrow(String message) throws Exception {
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
 
    int arrayLoad(int x) {
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

    boolean instanceOf() throws Exception {
        return n instanceof Double;
    }

    Double checkcast() throws Exception {
        return (Double) n;
    }
    
    void fastSynchronized() {
        synchronized (o) {
            System.out.println("ok");
        }
    }

    void tryCatchFinally() {
        try {
            System.out.println("try");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("catch");
        } finally {
            System.out.println("finally");
        }
    }
    
    int preInc() {
        return a[++x];
    }

    int postInc() {
        return a[x++];
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

    int ternaryOp(boolean flag) {
        return flag ? x : y;
    }

    int complexStore() {
        x = n instanceof Double ? x + y: -a.length;
        return x;
    }
}
