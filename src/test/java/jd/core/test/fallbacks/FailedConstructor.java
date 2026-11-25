package jd.core.test.fallbacks;

/**
 * The first constructor cannot be reconstructed yet: it must be written as byte code
 * without preventing the decompilation of the rest of the class.
 */
public class FailedConstructor {
    String name;
    int size = 3;

    FailedConstructor(String s) {
        this.name = switch (s) {
            case "a" -> "A";
            case "b" -> "B";
            default -> throw new IllegalStateException();
        };
    }

    FailedConstructor() {
        System.out.println();
    }
}
