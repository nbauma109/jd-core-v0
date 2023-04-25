package jd.core.test;

public class OuterAcessor {

    int i;
    
    void init() {
    }

    class Inner {

        int i;

        void init() {
            new Thread() {
                int i;

                @Override
                public void run() {
                    OuterAcessor.this.init();
                    int i = 0;
                    this.i = i;
                }
            }.start();
        }
    }
}
