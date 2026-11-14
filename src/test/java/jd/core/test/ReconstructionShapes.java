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

    /** Control characters that need an escape sequence in a string constant. */
    static final String CONTROLS = "backspace\b formfeed\f tab\t newline\n return\r quote\" backslash\\";

    /** A try with only a finally, and a nested synchronized block, inside a synchronized block. */
    int nested(List<Integer> values, Object other) {
        synchronized (values) {
            synchronized (other) {
                try {
                    values.add(1);
                } finally {
                    other.notifyAll();
                }
            }
            return values.size();
        }
    }

    /** The result is discarded where it is called. */
    private int touch() {
        return counter++;
    }

    /** The value that is returned is stored before the finally block, whatever its type. */
    long returnedLong(long value) {
        try {
            return value * 2;
        } finally {
            touch();
        }
    }

    double returnedDouble(double value) {
        try {
            return value / 2;
        } finally {
            touch();
        }
    }

    float returnedFloat(float value) {
        try {
            return value + 1;
        } finally {
            touch();
        }
    }

    Object returnedObject(Object value) {
        try {
            return value;
        } finally {
            touch();
        }
    }

    /** An array initializer whose values are calls on their own lines. */
    static String[] calls(int a, int b) {
        return new String[] {
                String.valueOf(a),
                String.valueOf(b),
                String.valueOf(a + b)
        };
    }

    /** A try with a finally and no value, inside a synchronized block. */
    void finallyInSynchronized(Object lock) {
        synchronized (lock) {
            try {
                touch();
            } finally {
                touch();
            }
        }
    }

    /** Conditions whose operands are conditions with ternary operators. */
    boolean ternaryConditions(int x, int y, int z) {
        boolean result = (x > 0 ? y > 0 : z > 0) && (y > 5 ? z > 5 : x > 5);
        if (x > 0 ? y > 0 : y < 0 && x < -3) {
            x++;
        }
        return result;
    }

    /** Arrays whose values are calls written on their own lines. */
    Object[] spreadCalls(Object a, int b, String c) {
        String[] names = {
                c.trim(),
                c.trim() };
        int[] numbers = {
                b + 1,
                b + 2 };
        return new Object[] {
                a,
                numbers.length,
                c.toUpperCase() };
    }
}
