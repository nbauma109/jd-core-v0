package jd.core.test;
/*
 * A test input for the string switch pattern.
 */
public class StringSwitch {

    void stringSwitch(String inputValue) {
        switch (inputValue) {
            case "1":
                System.out.println("One");
                break;
            case "2":
                System.out.println("Two");
                break;
            case "3":
                System.out.println("Three");
                break;
            default:
                throw new IllegalArgumentException();
        }
    }
}
