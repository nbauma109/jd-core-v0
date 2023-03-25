package jd.core.test;

public class OverrideAnnotation {

    class A {
        void testDefault() {
        }

        private void testPrivate() {
        }

        public void testPublic() {
            testPrivate();
        }

        protected void testProtected() {
        }

        static void testStatic() {
        }
    }

    class B extends A {
        void testDefault() {
        }

        private void testPrivate() {
        }

        public void testPublic() {
            testPrivate();
        }

        protected void testProtected() {
        }

        static void testStatic() {
        }
    }
}
