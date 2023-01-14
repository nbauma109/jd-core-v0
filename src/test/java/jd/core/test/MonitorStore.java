package jd.core.test;

public class MonitorStore {

    void test(String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            synchronized (e.getStackTrace()) {
                e.printStackTrace();
            }
        }
    }
}
