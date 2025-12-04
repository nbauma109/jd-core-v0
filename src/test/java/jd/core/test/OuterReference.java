package jd.core.test;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class OuterReference {

    int[] a;
    int x, y;
    Number d;
    Object o;
    Point p;
    boolean flag;
    static Map<Integer, Integer> m;

    class Inner {

        int[] newInitArray = { x, y };

        int arrayLength() {
            return a.length;
        }

        Inner() {
            m = new HashMap<>(); // PUTSTATIC
            m.put(x, y); // POP
            a = new int[] { x, y }; // NEWINITARRAY
            a[x++] = a.length + y; // ARRAYSTORE, ARRAYLENGTH, BINARYOP
            a[++y] = -x; // UNARYOP
            a[x + y] = d instanceof Double d2 ? (int) Math.round(d2) : x; // CONVERT
            p.y = flag ? ++p.x : --p.y; // TERNARYOPSTORE
            // TABLESWITCH
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
            // LOOKUPSWITCH
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
            // MONITORENTER/EXIT
            synchronized (o) {
                if (flag && d instanceof Double || (x < y && y < a.length)) { // IFCMP
                    System.out.println("ok");
                }
            }
            Object[][] multiANewArray = new Object[x][y]; // MULTIANEWARRAY
            multiANewArray[0] = new Object[y]; // ANEWARRAY
            multiANewArray[0][1] = new int[x++]; // NEWARRAY
            int i = 0;
            System.out.println(multiANewArray[i++]); // POSTINC
            System.out.println(multiANewArray[++i]); // PREINC
        }
    }
}
