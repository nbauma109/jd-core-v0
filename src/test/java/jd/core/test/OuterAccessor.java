package jd.core.test;

public class OuterAccessor {
    @SuppressWarnings("unused")
    void init(int i) {
    }

    class Inner {
        @SuppressWarnings("unused")
        void init(int i) {
            new Thread() {
                @Override
                public void run() {
                    int i = 0;
                    OuterAccessor.this.init(i++);
                }
            }.start();
        }
    }
}
