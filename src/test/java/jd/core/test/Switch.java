package jd.core.test;

public class Switch {
    public void testSwitch() {
        System.out.println("start");

        switch ((int) (System.currentTimeMillis() & 0xF)) {
            case 0:
                System.out.println("0");
            case 1:
                System.out.println("0 or 1");
                break;
            case 2:
            default:
                System.out.println("2 or >= 9");
                break;
            case 3:
            case 4:
                System.out.println("3 or 4");
                break;
            case 5:
                System.out.println("5");
                break;
            case 6:
                System.out.println("6");
                break;
            case 7:
                System.out.println("7");
                break;
            case 8:
                System.out.println("8");
                break;
        }

        System.out.println("end");
    }
}
