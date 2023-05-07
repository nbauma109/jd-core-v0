package jd.core.test;

public class TestInnerClass {
    public class InnerClass {
        private class InnerInnerClass {
            private int secretValue;

            private class InnerInnerInnerClass {

                public int getSecretValue() {
                    return InnerInnerClass.this.secretValue;
                }
            }
        }
    }
}
