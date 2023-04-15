package jd.core.test;

import java.io.Externalizable;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.function.Consumer;

public interface Generic {
    <T extends Serializable, X extends Runnable, Y extends Comparable<T>> void process(T obj, X x, Y y);
    <T extends Serializable & Runnable, X extends Comparable<T> & Cloneable & Externalizable> void process(T obj);
    <E> boolean addAll(Collection<? extends E> c);
    default <E> void forEach(@SuppressWarnings("unused") Consumer<? super E> action) {}
    <X extends Throwable> void throwGeneric() throws X;
    <X extends IOException, Y extends ClassNotFoundException> void throw2Generics() throws X, Y;
    <X extends ClassNotFoundException> void throwMixed() throws X, IOException;
}
