package jd.core.test;

public class OuterGetStatic {

    static int[] a;

    class Inner {

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

        int binaryOp() {
            return Float.class.hashCode() + Double.class.hashCode();
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
