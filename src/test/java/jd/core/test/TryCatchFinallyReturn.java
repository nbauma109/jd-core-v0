package jd.core.test;

public class TryCatchFinallyReturn {

    void tryCatchFinallyReturnInFinally(boolean flag) {
        if (flag) {
            try {
                System.out.println("try");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                return;
            }
        }
        System.out.println("end");
    }
}
