package jd.core.test;

import java.util.AbstractMap.SimpleEntry;

public class StringBuxxxer {

    class A<K, V> extends SimpleEntry<K, V> {

        private static final long serialVersionUID = 1L;

        public A(K key, V value) {
            super(key, value);
        }

        public String toString() {
            return new StringBuilder().append(getKey()).append('=').append(getValue()).toString();
        }
    }

    class B<K, V> extends SimpleEntry<K, V> {

        private static final long serialVersionUID = 1L;

        public B(K key, V value) {
            super(key, value);
        }

        public String toString() {
            return new StringBuilder().append(getKey()).append("=").append(getValue()).toString();
        }
    }

    class C<K, V> extends SimpleEntry<K, V> {

        private static final long serialVersionUID = 1L;

        public C(K key, V value) {
            super(key, value);
        }

        public String toString() {
            return new StringBuilder(16).append(getKey()).append("=").append(getValue()).toString();
        }
    }

    class D<K, V> extends SimpleEntry<K, V> {

        private static final long serialVersionUID = 1L;

        public D(K key, V value) {
            super(key, value);
        }

        public String toString() {
            return new StringBuilder(128).append(getKey()).append("=").append(getValue()).toString();
        }
    }

    class E<K, V> extends SimpleEntry<K, V> {

        private static final long serialVersionUID = 1L;

        public E(K key, V value) {
            super(key, value);
        }

        public String toString() {
            return new StringBuilder(Integer.MAX_VALUE).append(getKey()).append("=").append(getValue()).toString();
        }
    }

    class F<K, V> extends SimpleEntry<K, V> {

        private static final long serialVersionUID = 1L;

        public F(K key, V value) {
            super(key, value);
        }

        public String toString() {
            return new StringBuilder("").append(getKey()).append("=").append(getValue()).toString();
        }
    }
}
