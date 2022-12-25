package jd.core.test;

import java.awt.Point;
import java.util.Map;

public class OuterReference {

    int[] a;
    int x, y;
    Double d;
    Object o;

    class Inner {

        int[] newInitArray = { x, y };
        int length;

        Inner() {
            length = a.length;
        }

        int arrayLength() {
            return a.length;
        }

        void arrayStore() {
            a[x] = y;
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

        void tableSwitch() {
            switch (x) {
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
            switch (x) {
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
}
