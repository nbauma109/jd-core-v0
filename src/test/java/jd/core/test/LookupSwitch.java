package jd.core.test;
/*
 * TODO FIXME No need to import java.io.PrintStream
 */
public class LookupSwitch {

    void lookupSwitch(int inputValue) {
        switch (inputValue) {
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
}
