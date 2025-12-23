package jd.core.test;

import java.io.Serializable;

public class DotClass118 {

    Class clazz;
    static Class staticClass;
    static final Class CONST = Object.class;
    static final Class ARRAY_CONST = Object[].class;

    static class Inner {
        boolean check(String className) throws ClassNotFoundException {
            return Object.class.equals(Class.forName(className));
        }
    }

    boolean getField() {
        return clazz.equals(Object.class);
    }

    boolean preInc(Object[] array, int i) {
        return array[++i] == Object.class;
    }

    boolean postInc(Object[] array, int i) {
        return array[i++] == Object.class;
    }

    boolean arrayLoad(Object[] array) {
        return array[0].getClass() == Object.class;
    }

    Class xReturn() {
        return Object.class;
    }

    Class ldc(int dim) {
        return dim == 0 ? CONST : ARRAY_CONST;
    }

    void putField() {
        clazz = Object.class;
    }

    void putStatic() {
        staticClass = Object.class;
    }

    void aThrow() {
        throw new RuntimeException(String.valueOf(Object.class));
    }

    Class instanceOf(Class c) {
        if (Object.class instanceof Class) {
            return c;
        }
        return null;
    }

    Class ifCmp(Class c) {
        if (Object.class == c) {
            return c;
        }
        if (c == Object.class) {
            return c;
        }
        return null;
    }

    void aStore() {
        Class c = Object.class;
    }

    Object aaStore() {
        return new Class[] { String[].class };
    }

    int unaryOp() {
        return -Object.class.hashCode();
    }

    int binaryOp() {
        return Object.class.hashCode() + Object.class.hashCode();
    }

    void invokeStatic() {
        print(Object.class);
    }

    void invokeVirtual() {
        System.out.println(Object.class);
    }

    void checkCast() {
        clazz = (Class) readValue(Object.class);
    }

    void pop() {
        readValue(Object.class);
    }

    Class ternaryOpStore(String subtypeName) throws ClassNotFoundException {
        return (subtypeName == null) ? Object.class : Class.forName(subtypeName);
    }

    Class dupStore(Class c) {
        Class tmp;
        return tmp = c == (tmp = Object.class) ? (tmp = int.class) : (tmp = long.class);
    }

    static native Serializable readValue(Class expectedType);

    static native void print(Class c);
}
