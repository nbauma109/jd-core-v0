package jd.core.test;

public class TestSynchronize {

    static final String CONST1 = "1";
    static final String CONST2 = "2";

    private native Object getMonitor();

    // Byte code:
    // 0: getstatic 10 java/lang/System:out Ljava/io/PrintStream;
    // 3: ldc 1
    // 5: invokevirtual 11 java/io/PrintStream:println
    // (Ljava/lang/String;)V
    // 8: aload_0
    // 9: astore_1
    // 10: aload_1
    // 11: monitorenter
    // 12: aload_0
    // 13: invokespecial 8 TestSynchronize:getMonitor
    // ()Ljava/lang/Object;
    // 16: astore 4
    // 18: aload 4
    // 20: monitorenter
    // 21: getstatic 10 java/lang/System:out Ljava/io/PrintStream;
    // 24: ldc 2
    // 26: invokevirtual 11 java/io/PrintStream:println
    // (Ljava/lang/String;)V
    // 29: iconst_1
    // 30: istore_3
    // 31: jsr +12 -> 43
    // 34: jsr +19 -> 53
    // 37: iload_3
    // 38: ireturn
    // 39: aload 4
    // 41: monitorexit
    // 42: athrow
    // 43: astore 5
    // 45: aload 4
    // 47: monitorexit
    // 48: ret 5
    // 50: aload_1
    // 51: monitorexit
    // 52: athrow
    // 53: astore_2
    // 54: aload_1
    // 55: monitorexit
    // 56: ret 2
    int trySynchronize() throws Exception {
        try {
            System.out.println(CONST1);
            synchronized (this) {
                synchronized (getMonitor()) {
                    System.out.println(CONST2);
                    return 1;
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    float trySynchronizeFloat() throws Exception {
        try {
            System.out.println(CONST1);
            synchronized (this) {
                synchronized (getMonitor()) {
                    System.out.println(CONST2);
                    return 1f;
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
