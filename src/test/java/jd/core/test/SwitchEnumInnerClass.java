package jd.core.test;

public class SwitchEnumInnerClass {

    enum DataElement {
        KEY, VALUE;
    }

    String getString(DataElement dataElement) {
        return switch (dataElement) {
		case KEY -> "key";
		case VALUE -> "value";
		default -> throw new IllegalArgumentException();
		};
    }

    class Node {
        String getString(DataElement dataElement) {
            return switch (dataElement) {
			case KEY -> "key";
			case VALUE -> "value";
			default -> throw new IllegalArgumentException();
			};
        }
    }
}
