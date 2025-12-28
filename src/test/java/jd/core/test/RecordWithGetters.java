package jd.core.test;

public record RecordWithGetters(String a, double d) {

    public String getA() {
        return a;
    }

    public double getD() {
        return d;
    }
}
