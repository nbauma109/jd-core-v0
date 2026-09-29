package jd.core.test;

public class ExplicitOrdinalSwitch {
    enum Colour { RED, GREEN }

    int value(Colour colour) {
        // The bytecode has the same shape as javac's direct enum switch.
        switch (colour.ordinal()) {
            case -1:
                return -1;
            case 0:
                return 0;
            case 100:
                return 100;
            default:
                return 1;
        }
    }
}
