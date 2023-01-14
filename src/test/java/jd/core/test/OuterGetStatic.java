package jd.core.test;

import java.lang.reflect.InvocationTargetException;

public class OuterGetStatic {

    static int[] a;
    static int x, y;

    class Inner {

        int[] newInitArray = { x, y };

        void athrow() throws Exception {
            throw new Exception("Cannot convert " + Object.class + " to " + Void.class);
        }

        int arrayLength() {
            return new Class[] { Float.class }.length;
        }

        void arrayStore() {
            a[Double.class.hashCode()] = Float.class.hashCode();
        }

        int unaryOp() {
            return -Double.class.hashCode();
        }

        void instanceOf() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
            if (newInstance(Object.class) instanceof String) {
                System.out.println("ok");
            }
        }
        
        Object newInstance(Class c) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
            return c.getConstructor(new Class[0]).newInstance(new Object[0]);
        }

        int binaryOp() {
            return Float.class.hashCode() + Double.class.hashCode();
        }

        int preIncPostInc() {
            return x > 0 ? ++y : x++;
        }
        
        byte convert() {
            return (byte) a[Byte.class.hashCode()];
        }

        void tableSwitch() {
            switch (Object.class.hashCode()) {
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
            switch (Object.class.hashCode()) {
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

        Object[] aNewArray() {
            return new Object[Object.class.hashCode()];
        }

        int[] newArray() {
            return new int[Object.class.hashCode()];
        }

        Object[][] multiANewArray() {
            return new Object[Object.class.hashCode()][Object.class.hashCode()];
        }
        
        void dupStore() {
            synchronized (Object.class.toString()) {
                System.out.println("ok");
            }
        }
    }
}
