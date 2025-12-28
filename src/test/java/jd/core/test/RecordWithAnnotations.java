package jd.core.test;

public record RecordWithAnnotations(@Sensitive("C1") String a, @Sensitive("C0") double d) {
}