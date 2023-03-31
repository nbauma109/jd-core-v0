package jd.core.test;

/*
 * A test input for TABLESWITCH pattern.
 */
public class TableSwitch {
    int inputValue;

    void tableSwitch() {
        switch (inputValue++) {
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

    public void tableSwitch2(int value) {
        switch (value) {
            case 1:
                System.out.println("One");

                break;

            case 2:
                System.out.println("Two");

                break;

            case 4:
                System.out.println("Four");

                break;

            case 8:
                System.out.println("Eight");

                break;
            case 3: case 5: case 6:
            case 7: default:
                throw new IllegalArgumentException();
        }
    }

    public void tableSwitch3(char value) {
        switch (value) {
            case '1':
                System.out.println("One");

                break;

            case '2':
                System.out.println("Two");

                break;

            case '4':
                System.out.println("Four");

                break;

            case '8':
                System.out.println("Eight");

                break;
            case '3': case '5': case '6':
            case '7': default:
                throw new IllegalArgumentException();
        }
    }
}
