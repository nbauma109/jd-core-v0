package jd.core.test;

public record RecordWithGetters2(String a, double d) {

    public String a() {
        return a;
    }

    public double d() {
        return d;
    }
}
