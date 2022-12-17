package jd.core.test;

public class EmptySynchronizedBlock {

    // Cas particulier des blocks synchronises vides avec le
    // jdk 1.1.8.
    // Byte code++:
    // 3: monitorenter;
    // 10: monitorexit;
    // 11: return contentEquals(paramStringBuffer);
    // 12: localObject = finally;
    // 14: monitorexit;
    // 16: throw localObject;
    boolean contentEquals(StringBuffer paramStringBuffer) throws Throwable {
        try {
            synchronized (this) {
            }
            return contentEquals(paramStringBuffer);
        } catch (Throwable localObject) {
            throw localObject;
        }
    }

    // 5: System.out.println("start");
    // 9: localTestSynchronize = this;
    // 11: monitorenter;
    // 14: monitorexit;
    // 15: goto 21;
    // 19: monitorexit;
    // 20: throw finally;
    void trySynchronized() throws Throwable{
        try {
            System.out.println("start");
            Object localTestSynchronize = this;
            synchronized (localTestSynchronize) {
            }
        } catch (Throwable localObject) {
            throw localObject;
        }
    }
}
