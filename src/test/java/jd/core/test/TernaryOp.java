package jd.core.test;

import java.awt.Component;
import java.awt.Point;
import java.util.Map;

public class TernaryOp {

    Point p;

    static int[] NEW_INIT_ARRAY = { Math.random() > 0.5 ? 1 : 0 };
    static int i;

    int arrayLength(int[] a, int[] b) {
        return (Math.random() > 0.5 ? a : b).length;
    }

    void arrayStore(int[] a) {
        a[Math.random() > 0.5 ? 1 : 0] = 1;
    }

    void athrow(Throwable cause1, Throwable cause2) throws Exception {
        throw new Exception(Math.random() > 0.5 ? cause1 : cause2);
    }

    int unaryOp(int a, int b) throws Exception {
        return -(Math.random() > 0.5 ? a : b);
    }

    int binaryOp(int a, int b) throws Exception {
        return (Math.random() > 0.5 ? a : b) + (Math.random() > 0.5 ? a : b);
    }

    byte convert() throws Exception {
        return (byte) (Math.random() > 0.5 ? 1 : 0);
    }

    void putfield() {
        p.y = Math.random() > 0.5 ? ++p.x : --p.y;
    }

    void instanceOf(Object a, Object b) throws Exception {
        if ((Math.random() > 0.5 ? a : b) instanceof Number) {
            System.out.println("ok");
        }
    }

    void tableSwitch(int a, int b) {
        switch (Math.random() > 0.5 ? a : b) {
            case 1:
                System.out.println("One");
                break;
            case 2:
                System.out.println("Two");
                break;
            case 3:
                System.out.println("Three");
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    void lookupSwitch(int a, int b) {
        switch (Math.random() > 0.5 ? a : b) {
            case 1:
                System.out.println("One");
                break;
            case 1000:
                System.out.println("One thousand");
                break;
            case 1000000:
                System.out.println("One million");
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    void fastSynchronized(Object a, Object b) {
        synchronized (Math.random() > 0.5 ? a : b) {
            System.out.println("ok");
        }
    }

    Object tryResources(AutoCloseable a, AutoCloseable b) {
        Object o = null;
        try (AutoCloseable ac = Math.random() > 0.5 ? a : b) {
            System.out.println(ac);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }

    Object[] aNewArray() {
        return new Object[Math.random() > 0.5 ? 1 : 0];
    }

    int[] newArray() {
        return new int[Math.random() > 0.5 ? 1 : 0];
    }

    Object[][] multiANewArray() {
        return new Object[Math.random() > 0.5 ? 1 : 0][Math.random() > 0.5 ? 1 : 0];
    }

    static void putStatic(int a, int b) {
        i = Math.random() > 0.5 ? a : b;
    }

    void pop(Map<String, String> m1, Map<String, String> m2) {
        (Math.random() > 0.5 ? m1 : m2).put("key", "value");
    }

    boolean isVisible(Component comp) {
        return (comp != null) ? comp.isVisible() : false;
    }
}
