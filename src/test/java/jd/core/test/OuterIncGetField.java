package jd.core.test;

import org.apache.commons.collections4.Bag;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class OuterIncGetField<E> implements Bag<E> {

    private transient Map<E, MutableInteger> map;
    private int size;
    private transient int modCount;

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int getCount(final Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(final Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(final Collection<?> coll) {
        throw new UnsupportedOperationException();
    }
    @Override
    public Iterator<E> iterator() {
        return new BagIterator<>(this);
    }

    static class BagIterator<E> implements Iterator<E> {
        private final OuterIncGetField<E> parent;
        private final Iterator<Map.Entry<E, MutableInteger>> entryIterator;
        private Map.Entry<E, MutableInteger> current;
        private final int mods;
        private boolean canRemove;

        public BagIterator(final OuterIncGetField<E> parent) {
            this.parent = parent;
            this.entryIterator = parent.map.entrySet().iterator();
            this.current = null;
            this.mods = parent.modCount;
            this.canRemove = false;
        }

        @Override
        public boolean hasNext() {
            throw new UnsupportedOperationException();
        }

        @Override
        public E next() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove() {
            if (parent.modCount != mods) {
                throw new ConcurrentModificationException();
            }
            if (canRemove == false) {
                throw new IllegalStateException();
            }
            final MutableInteger mut = current.getValue();
            if (mut.value > 1) {
                mut.value--;
            } else {
                entryIterator.remove();
            }
            --parent.size;
            canRemove = false;
        }
    }

    @Override
    public boolean add(final E object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(final E object, final int nCopies) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(final Collection<? extends E> coll) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(final Object object, final int nCopies) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(final Collection<?> coll) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(final Collection<?> coll) {
        throw new UnsupportedOperationException();
    }

    protected static class MutableInteger {
        protected int value;

        MutableInteger(final int value) {
            this.value = value;
        }

        @Override
        public boolean equals(final Object obj) {
            return false;
        }

        @Override
        public int hashCode() {
            return value;
        }
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<E> uniqueSet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(final Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }

}
