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

            case "4":
                System.out.println("Four");

                break;

            case "8":
                System.out.println("Eight");

                break;
            case "3": case "5": case "6":
            case "7": default:
                throw new IllegalArgumentException();
        }
    }
}
