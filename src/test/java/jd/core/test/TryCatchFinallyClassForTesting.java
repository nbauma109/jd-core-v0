package jd.core.test;

public class TryCatchFinallyClassForTesting {

    // Retrait de la sous procédure allant de "monitorexit" à "ret"
    // Byte code:
    // 0: aload_1
    // 1: astore_3
    // 2: aload_3
    // 3: monitorenter <----- tryFromIndex
    // 4: aload_0
    // 5: invokevirtual 6 TryCatchFinallyClassForTest:inTry ()V
    // 8: iconst_2
    // 9: istore_2
    // 10: jsr +8 -> 18
    // 13: iload_2
    // 14: ireturn
    // 15: aload_3 <===== finallyFromOffset
    // 16: monitorexit
    // 17: athrow
    // 18: astore 4 <~~~~~ entrée de la sous procecure ('jsr')
    // 20: aload_3
    // 21: monitorexit
    // 22: ret 4 <-----
    int trySynchronized1(Object o) throws Exception {
        try {
            synchronized (o) {
                inTry();
                return 2;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    native void inTry();
}
