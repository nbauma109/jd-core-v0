package jd.core.test;

public class Wait {

    boolean initialized;

    void waitInit(long startTime, long timeout) {
        while (!initialized) {
            if (System.currentTimeMillis() - startTime > timeout * 1000L) {
                throw new RuntimeException();
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    synchronized void waitInit() {
        while (!initialized) {
            try {
                this.wait();
            } catch (final InterruptedException ex) {}
        }
        this.initialized = false;
    }
}
