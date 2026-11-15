package jd.core.test;

/**
 * The last statement of a try block that looks like the statement of the finally block, but is another one: it is not
 * a copy of the finally block and must stay in the try block.
 */
public class FinallyLookAlikes {

    static int shared;
    static int other;

    static int first() {
        return shared++;
    }

    static int second() {
        return shared--;
    }

    static void log(String message) {
        shared += message.length();
    }

    static void use(long value) {
        shared += (int) value;
    }

    int differentMethods() {
        try {
            first();
            return 0;
        } finally {
            second();
        }
    }

    int differentStaticFieldValues() {
        try {
            shared = 1;
            return 0;
        } finally {
            shared = 2;
        }
    }

    int differentStaticFields() {
        try {
            use(shared);
            return 0;
        } finally {
            use(other);
        }
    }

    int differentConstants() {
        try {
            log("a");
            return 0;
        } finally {
            log("b");
        }
    }

    int differentObjects() {
        try {
            new StringBuilder("a");
            return 0;
        } finally {
            new StringBuilder("b");
        }
    }

    int differentIncrements() {
        int i = 0;
        try {
            i++;
            return i;
        } finally {
            i += 2;
        }
    }

    long differentLongs(long v, long w) {
        try {
            use(v);
            return v;
        } finally {
            use(w);
        }
    }

    int differentMonitors(Object a, Object b) {
        try {
            synchronized (a) {
                shared++;
            }
            return 0;
        } finally {
            synchronized (b) {
                shared++;
            }
        }
    }
}
