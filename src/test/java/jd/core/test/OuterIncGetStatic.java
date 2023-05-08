package jd.core.test;

public abstract class OuterIncGetStatic {

    private static int modCount;

    static class BagIterator {

        public int remove() {
            --modCount; // inc/getstatic
            return modCount;
        }
    }
}