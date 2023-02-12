package jd.core.test;

public class EmptyLambda {

    void start() {
        new Thread(() -> {}).start();
    }
}
