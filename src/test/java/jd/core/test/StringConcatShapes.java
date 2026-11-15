package jd.core.test;

/**
 * Compiled for Java 8 (see the jar in the test resources): the concatenation is a chain of
 * StringBuilder calls, also in the message of an assertion.
 */
public class StringConcatShapes {

    private final String name;

    public StringConcatShapes(String name) {
        this.name = name;
    }

    int check(int value) {
        assert value > 0 : "value " + value + " of " + name;
        return value;
    }
}
