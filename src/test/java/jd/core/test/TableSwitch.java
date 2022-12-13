package jd.core.test;
/*
 * TODO FIXME No need to import java.io.PrintStream
 */
public class TableSwitch {

    void tableSwitch(int inputValue) {
        switch (inputValue) {
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
