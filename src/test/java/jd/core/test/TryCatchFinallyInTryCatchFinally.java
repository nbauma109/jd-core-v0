package jd.core.test;

import java.util.Random;

public class TryCatchFinallyInTryCatchFinally {

    public int methodTryCatchFinallyInTryCatchFinally(Random r) throws Exception {
        System.out.println("start");

        int a = 1;
        int b = 1;

        try {
            System.out.println("in try");

            try {
                System.out.println("in inner try");

                if (r.nextBoolean()) {
                    System.out.println("before throw in inner try");
                    throw new RuntimeException();
                }

                return b;
            } catch (RuntimeException e) {
                System.err.println(e);

                if (r.nextBoolean()) {
					throw new RuntimeException();
				}

                System.out.println("in catch in inner try");
            } finally {
                System.out.println("in finally in inner try");
            }

            System.out.println("in try");

            try {
                System.out.println("in inner try");

                if (r.nextBoolean()) {
                    System.out.println("before throw in inner try");
                    throw new RuntimeException();
                }

                return b;
            } catch (RuntimeException e) {
                e.printStackTrace();

                if (r.nextBoolean()) {
					throw new RuntimeException();
				}

                System.out.println("in catch in inner try");
            } finally {
                System.out.println("in finally in inner try");
            }

            System.out.println("in try");
        } finally {
            System.out.println("in finally");
        }

        System.out.println("end");

        return a + b;
    }
}
