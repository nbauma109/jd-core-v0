package jd.core.test;
/*
 * A test input for the enum switch pattern.
 */
public class EnumSwitch {

    static enum Digit {
        _0, _1, _2, _3, _4, _5, _6, _7, _8, _9
    }

    void stringSwitch(Digit inputValue) {
        switch (inputValue) {
            case _1:
                System.out.println("One");

                break;

            case _2:
                System.out.println("Two");

                break;

            case _4:
                System.out.println("Four");

                break;

            case _8:
                System.out.println("Eight");

                break;
            case _3: case _5: case _6:
            case _7: default:
                throw new IllegalArgumentException();
        }
    }
}
