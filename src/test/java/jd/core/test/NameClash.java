package jd.core.test;

public class NameClash {

    abstract class HashMap<K, V> extends java.util.HashMap<K, V> {
        private static final long serialVersionUID = 1L;
    }

    abstract class Map<K, V> implements java.util.Map<K, V> {
    }
}
