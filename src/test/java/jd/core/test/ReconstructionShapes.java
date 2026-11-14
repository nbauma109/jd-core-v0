package jd.core.test;

import java.util.ArrayList;
import java.util.List;

/**
 * Shapes of code that the reconstructions must keep handling whatever the library used as input:
 * they were covered by the bytecode of third-party classes only.
 */
public abstract class ReconstructionShapes {

    /** An abstract method declared before the constructor, which initializes an instance field. */
    protected abstract int compute();

    private int counter = 5;

    protected ReconstructionShapes() {
        counter++;
    }

    /** A static initializer that starts with a call: the array initializer is found from the end. */
    static final List<String> LOG = new ArrayList<>();

    static {
        LOG.add("loaded");
    }

    static final int[] PRIMES = { 2, 3, 5, 7 };

    /** An array initializer written on many lines. */
    static final String[] NAMES = {
            "first",
            "second",
            "third",
            "fourth",
            "fifth",
            "sixth"
    };

    /** A try block with a catch and a finally inside a synchronized block. */
    int guarded(List<Integer> values) {
        synchronized (values) {
            try {
                return values.get(0) + counter;
            } catch (IndexOutOfBoundsException e) {
                return -1;
            } finally {
                values.clear();
            }
        }
    }

    /** The message of an assertion can be rewritten. */
    void asserted(int value, Integer boxed, List<String> names) {
        assert value > 0 : boxed;
        assert value < 10 : names.get(0);
        assert value != 5 : (Object) Integer.valueOf(value);
    }

    /** An array initializer whose first value is on another line than the declaration. */
    int[] spread(int first, int second) {
        int[] values = {
                Math.abs(first),
                Math.abs(second) };
        int[] inline = { first, second };
        return values.length > 1 ? values : inline;
    }
}
