package jd.core.test;

public abstract class Dup2X2Example {

    protected abstract void form1Result(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6);

    protected abstract void form2Result(long paramLong1, int paramInt1, int paramInt2, long paramLong2);

    protected abstract void form3Result(int paramInt1, int paramInt2, long paramLong, int paramInt3, int paramInt4);

    protected abstract void form4Result(long paramLong1, long paramLong2, long paramLong3);

    // ..., value4, value3, value2, value1 → ..., value2, value1, value4, value3, value2, value1
    public void form1(int value4, int value3, int value2, int value1) {
        form1Result(value2, value1, value4, value3, value2, value1);
    }

    // ..., value3, value2, value1 → ..., value1, value3, value2, value1
    public void form2(int value3, int value2, long value1) {
        form2Result(value1, value3, value2, value1);
    }

    // ..., value3, value2, value1 → ..., value2, value1, value3, value2, value1
    public void form3(long value3, int value2, int value1) {
        form3Result(value2, value1, value3, value2, value1);
    }

    // ..., value2, value1 → ..., value1, value2, value1
    public void form4(long value2, long value1) {
        form4Result(value1, value2, value1);
    }
}
