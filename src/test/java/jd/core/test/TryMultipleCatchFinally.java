package jd.core.test;

public class TryMultipleCatchFinally {

    public void methodTryCatchCatchFinally() {
        before();
        try {
            inTry();
        } catch (RuntimeException localRuntimeException) {
            inCatch1();
        } catch (Exception localException) {
            inCatch2();
        } finally {
            inFinally();
        }

        after();
    }

    public void methodTryCatchCatchCatchFinally() {
        before();
        try {
            inTry();
        } catch (ClassCastException localClassCastException) {
            inCatch1();
        } catch (RuntimeException localRuntimeException) {
            inCatch2();
        } catch (Exception localException) {
            inCatch3();
        } finally {
            inFinally();
        }
        after();
    }

    private void before() {
    }

    private void inTry() {
    }

    private void inCatch1() {
    }

    private void inCatch2() {
    }

    private void inCatch3() {
    }

    private void inFinally() {
    }

    private void after() {
    }

}
