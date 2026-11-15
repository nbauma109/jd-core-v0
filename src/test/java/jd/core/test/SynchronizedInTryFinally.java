package jd.core.test;

/** A synchronized block in a try with a finally: the statements of the block are not the copies of the finally block. */
public class SynchronizedInTryFinally {

    int counter;

    int touch() {
        return counter++;
    }

    int other() {
        return counter--;
    }

    void different(Object lock) {
        try {
            synchronized (lock) {
                other();
            }
        } finally {
            touch();
        }
    }

    void same(Object lock) {
        try {
            synchronized (lock) {
                touch();
            }
        } finally {
            touch();
        }
    }

    void inSynchronized(Object lock, Object nested) {
        synchronized (lock) {
            try {
                synchronized (nested) {
                    other();
                }
            } finally {
                touch();
            }
        }
    }

    int returnedInside(Object lock) {
        try {
            synchronized (lock) {
                return other();
            }
        } finally {
            touch();
        }
    }

    void statementsAround(Object lock) {
        try {
            counter = 1;
            synchronized (lock) {
                other();
                counter = 2;
            }
            counter = 3;
        } finally {
            touch();
        }
    }

    int statementBeforeReturn() {
        try {
            other();
            return counter;
        } finally {
            touch();
        }
    }
}
