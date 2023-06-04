package jd.core.test;

public class BinaryOp {

    public int add(int i, int j, int k) {
        return i + j + k;
    }

    public int mul(int i, int j, int k) {
        return i * j * k;
    }
    
    public int sub(int i, int j, int k) {
        return i - (j + k);
    }

    public int div(int i, int j, int k) {
        return i / (j / k);
    }
}
