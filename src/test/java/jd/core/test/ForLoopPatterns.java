package jd.core.test;

public class ForLoopPatterns {
    private int i;

    // for (beforeLoop; ;)
    void beforeLoop() {
        int i = 0;
        for (;;) {
            System.out.println(i++);
            if (i < 10) {
            }
        }
    }

    // for (; ; lastBodyLoop)
    void lastBodyLoop() {
        for (;; i++) {
            System.out.println("start");
            System.out.println(i);
            System.out.println("end");
        }
    }

    // for (beforeLoop; ; lastBodyLoop)
    void beforeLooplastBodyLoop() {
        for (int i = 0;; i++) {
            System.out.println("start");
            System.out.println(i);
            System.out.println("end");
        }
    }

    // for (; test; lastBodyLoop)
    void testlastBodyLoop() {
        for (; i < 5; i++) {
            System.out.println(i);
        }
        for (; i < 10; i++) {
        }
    }
}
