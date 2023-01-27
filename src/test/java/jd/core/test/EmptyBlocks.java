package jd.core.test;

public class EmptyBlocks {

    void emptySynchronized() {
        synchronized (this) {
        }
    }

    void emptyIf(boolean flag) {
        if (flag) {}
    }
}
