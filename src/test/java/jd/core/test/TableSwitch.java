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
}
