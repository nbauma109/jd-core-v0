package jd.core.test;

public abstract class OuterIncGetField {

    private int size;

    static class BagIterator {
        private OuterIncGetField parent;

        public int remove() {
            --parent.size;
            return parent.size;
        }
    }
}