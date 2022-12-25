package jd.core.test;

public class TryFinallyReturnThrow {

    void tryFinallyReturnInFinally(boolean flag) {
        if (flag) {
            try {
                System.out.println("try");
            } finally {
                return;
            }
        }
        System.out.println("end");
    }

    void tryFinallyThrowInFinally(boolean flag) {
        if (flag) {
            try {
                System.out.println("try");
            } finally {
                throw new RuntimeException();
            }
        }
        System.out.println("end");
    }

}
