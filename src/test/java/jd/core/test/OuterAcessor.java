package jd.core.test;

public class OuterAcessor {

    void init() {
    }

    class Inner {

        void init() {
            new Thread() {
                @Override
                public void run() {
                    OuterAcessor.this.init();
                }
            }.start();
        }
    }
}
