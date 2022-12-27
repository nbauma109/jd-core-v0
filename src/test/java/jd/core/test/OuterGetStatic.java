package jd.core.test;

import java.awt.Point;
import java.util.Map;

public class OuterGetStatic {

    static int[] a;
    static int x, y;
    static Double d;
    static Object o;

    public static int[] getArray() {
        return a;
    }

    public static int getX() {
        return x;
    }

    public static int getY() {
        return y;
    }

    public static Double getDouble() {
        return d;
    }

    public static Object getObject() {
        return o;
    }

    class Inner {

        int[] newInitArray = { getX(), getY() };

        int arrayLength() {
            return getArray().length;
        }

        void arrayStore() {
            getArray()[getX()] = getY();
        }

        int unaryOp() {
            return -getX();
        }

        int binaryOp() {
            return getX() + getY();
        }

        byte convert() {
            return (byte) getX();
        }

        void instanceOf() throws Exception {
            if (getDouble() instanceof Number) {
                System.out.println("ok");
            }
        }

        void tableSwitch() {
            switch (getX()) {
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

        void lookupSwitch() {
            switch (getX()) {
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

        void fastSynchronized() {
            synchronized (getObject()) {
                System.out.println("ok");
            }
        }

        Object[] aNewArray() {
            return new Object[getX()];
        }

        int[] newArray() {
            return new int[getX()];
        }

        Object[][] multiANewArray() {
            return new Object[getX()][getY()];
        }
    }
}
