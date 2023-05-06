package jd.core.test;

public class PutStaticAccessor {

    private static Object theObject = null;

    static void init() {
        new Thread(new Runnable() {

            public void run() {
                if (theObject == null) {
                    theObject = new Object();
                }
            }
        });
    }
}
