package jd.core.test;
/*
 * A test input for LOOKUPSWITCH pattern.
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
