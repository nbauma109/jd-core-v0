package jd.core.test;

/*
 * https://github.com/mstrobel/procyon/wiki/Decompiler-Output-Comparison
 */
public class InnerClassMadness {

    public static void main(final String[] args) {
        InnerClassMadness t = new InnerClassMadness();
        final A a = t.new A();
        final A.B b = a.new B(a);
        final A.D d = a.new D(a, b);
        final A.D d2 = new Object() {
            class Inner {
                A.D getD() {
                    final InnerClassMadness t = new InnerClassMadness();
                    final A a = t.new A();
                    final A.B b = a.new B(a);
                    return a.new D(a, b);
                }
            }
        }.new Inner().getD();
    }

    public class A extends InnerClassMadness {
        public A() {
        }

        public class B extends A {
            public B(final A a) {
            }

            public class C extends B {
                public C(final A a) {
                    a.super(a);
                }
            }
        }

        public class D extends B.C {
            public D(final A a, final B b) {
                b.super(a);
            }
        }
    }
}