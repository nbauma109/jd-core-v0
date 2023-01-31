package jd.core.test;

public class InitInstanceFields {

    class A {
        Object o = null;

        A(Object o) {
            this();
            this.o = o;
        }

        A() {}
    }

    class B {
        boolean b = false;

        B(boolean b) {
            this();
            this.b = b;
        }

        B() {}
    }
}
