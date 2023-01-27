package jd.core.test;

public class TryCatchSynchronized {

    void tryCatchSynchronized() {
        try {
            System.out.println("try");
        } catch (Exception e) {
            synchronized (e.getStackTrace()) {
                System.out.println("catch");
            }
        }
    }
}
