package jd.core.test;

public record RecordWithConstructor(Object a, Object b) {

    public RecordWithConstructor {
        if (a == null || b == null) {
            throw new IllegalArgumentException("neither a nor b can be null");
        }
    }
}
