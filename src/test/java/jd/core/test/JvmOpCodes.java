package jd.core.test;

public class JvmOpCodes {
    long l1, l2;

    void dup2x1() {
        l2 = l1 = 1;
    }

    long dup2(long a) {
        return ++a;
    }

    long dup2x2(long[] array, int i, long l) {
        return array[i] = l;
    }

    int dupx2(int[] array, int i, int l) {
        return array[i] = l;
    }

    int ior(int a, int b) {
        return a | b;
    }

    long lor(long a, long b) {
        return a | b;
    }

    int land(int a, int b) {
        return a & b;
    }

    long land(long a, long b) {
        return a & b;
    }

    long lushr(long a, int b) {
        return a >>> b;
    }

    int iushr(int a, int b) {
        return a >>> b;
    }

    int ishl(int a, int b) {
        return a << b;
    }

    long lshl(long a, int b) {
        return a << b;
    }
    
    int ishr(int a, int b) {
        return a >> b;
    }
    
    long lshr(long a, int b) {
        return a >> b;
    }
    
    float fsub(float a, float b) {
        return a - b;
    }

    double dsub(double a, double b) {
        return a - b;
    }

    int isub(int a, int b) {
        return a - b;
    }

    long lsub(long a, long b) {
        return a - b;
    }

    float fadd(float a, float b) {
        return a + b;
    }

    double dadd(double a, double b) {
        return a + b;
    }

    int iadd(int a, int b) {
        return a + b;
    }

    long ladd(long a, long b) {
        return a + b;
    }

    float fmul(float a, float b) {
        return a * b;
    }

    double dmul(double a, double b) {
        return a * b;
    }

    int imul(int a, int b) {
        return a * b;
    }

    long lmul(long a, long b) {
        return a * b;
    }

    float fdiv(float a, float b) {
        return a / b;
    }

    double ddiv(double a, double b) {
        return a / b;
    }

    int idiv(int a, int b) {
        return a / b;
    }

    long ldiv(long a, long b) {
        return a / b;
    }

    float frem(float a, float b) {
        return a % b;
    }

    double drem(double a, double b) {
        return a % b;
    }

    int irem(int a, int b) {
        return a % b;
    }

    long lrem(long a, long b) {
        return a % b;
    }

    float fneg(float a) {
        return -a;
    }

    double dneg(double a) {
        return -a;
    }

    int ineg(int a) {
        return -a;
    }

    long lneg(long a) {
        return -a;
    }

    void pop() {
        Math.round(0.5f);
    }

    void pop2() {
        Math.round(0.5d);
    }
}
