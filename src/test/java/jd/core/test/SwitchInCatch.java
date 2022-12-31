package jd.core.test;

public class SwitchInCatch {

    void tableSwitch() {
        try {
            System.out.println("try");
        } catch (Exception e) {
            switch (e.hashCode()) {
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
            // TODO FIXME does not work without post-switch instruction
            System.out.println("after switch");
        }
    }

    void lookupSwitch() {
        try {
            System.out.println("try");
        } catch (Exception e) {
            switch (e.hashCode()) {
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
            // TODO FIXME does not work without post-switch instruction
            System.out.println("after switch");
        }
    }
}
