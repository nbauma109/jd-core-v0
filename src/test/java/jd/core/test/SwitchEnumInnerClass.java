package jd.core.test;

public class SwitchEnumInnerClass {

    enum DataElement {
        KEY, VALUE;
    }

    String getString(DataElement dataElement) {
        switch (dataElement) {
        case KEY:
            return "key";
        case VALUE:
            return "value";
        default:
            throw new IllegalArgumentException();
        }
    }

    class Node {
        String getString(DataElement dataElement) {
            switch (dataElement) {
            case KEY:
                return "key";
            case VALUE:
                return "value";
            default:
                throw new IllegalArgumentException();
            }
        }
    }
}
