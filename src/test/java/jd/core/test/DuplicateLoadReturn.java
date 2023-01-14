package jd.core.test;

public class DuplicateLoadReturn {

    int testILoad1() {
        int i = 0;
        label0: {
            while (while1Condition(i)) {
                while (while2Condition(i)) {
                    if (if1Condition(i)) {
                        break;
                    }
                    if (if2Condition(i)) {
                        break label0;
                    }
                }
            }
        }
        return i;
    }

    long testJLoad1() {
        long l = 0;
        label0: {
            while (while1Condition(l)) {
                while (while2Condition(l)) {
                    if (if1Condition(l)) {
                        break;
                    }
                    if (if2Condition(l)) {
                        break label0;
                    }
                }
            }
        }
        return l;
    }

    float testFLoad1() {
        float f = 0;
        label0: {
            while (while1Condition(f)) {
                while (while2Condition(f)) {
                    if (if1Condition(f)) {
                        break;
                    }
                    if (if2Condition(f)) {
                        break label0;
                    }
                }
            }
        }
        return f;
    }

    double testDLoad1() {
        double d = 0;
        label0: {
            while (while1Condition(d)) {
                while (while2Condition(d)) {
                    if (if1Condition(d)) {
                        break;
                    }
                    if (if2Condition(d)) {
                        break label0;
                    }
                }
            }
        }
        return d;
    }

    Object testALoad1() {
        Object o = 0;
        label0: {
            while (while1Condition(o)) {
                while (while2Condition(o)) {
                    if (if1Condition(o)) {
                        break;
                    }
                    if (if2Condition(o)) {
                        break label0;
                    }
                }
            }
        }
        return o;
    }

    @SuppressWarnings("unused")
    void testVoid() {
        label0: {
            while (while1Condition()) {
                while (while2Condition()) {
                    try {
                        System.out.println("in try");
                    } catch (Exception e) {
                        break label0;
                    }
                }
            }
        }
    }

    native boolean if1Condition(int i);

    native boolean if2Condition(int i);

    native boolean while1Condition();

    native boolean while2Condition();

    native boolean while1Condition(int i);
    
    native boolean while2Condition(int i);
    
    native boolean if1Condition(long i);

    native boolean if2Condition(long i);

    native boolean while1Condition(long i);

    native boolean while2Condition(long i);

    native boolean if1Condition(float i);

    native boolean if2Condition(float i);

    native boolean while1Condition(float i);

    native boolean while2Condition(float i);

    native boolean if1Condition(double i);

    native boolean if2Condition(double i);

    native boolean while1Condition(double i);

    native boolean while2Condition(double i);

    native boolean if1Condition(Object i);

    native boolean if2Condition(Object i);

    native boolean while1Condition(Object i);

    native boolean while2Condition(Object i);
}
