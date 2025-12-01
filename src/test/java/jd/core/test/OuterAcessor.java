package jd.core.test;

public class OuterAcessor {
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
                    OuterAcessor.this.init(i);
					i++;
                }
            }.start();
        }
    }
}
