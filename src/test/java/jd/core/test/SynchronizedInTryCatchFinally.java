package jd.core.test;

/** A synchronized block in a try with a catch and a finally (the shape that ecj generates is not reconstructed). */
public class SynchronizedInTryCatchFinally {

    int counter;

    int touch() {
        return counter++;
    }

    int other() {
        return counter--;
    }

    void withCatch(Object lock) {
        try {
            synchronized (lock) {
                other();
            }
        } catch (IllegalStateException e) {
            other();
        } finally {
            touch();
        }
    }
}
