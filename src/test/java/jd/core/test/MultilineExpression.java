package jd.core.test;

public class MultilineExpression {
    static class Builder {
        Builder append(String value) { return this; }
        String finish() { return ""; }
    }

    String build(String first, String second) {
        return new Builder()
                .append(first)
                .append(second)
                .finish();
    }

    int arguments() {
        return Math.max(
                Integer.parseInt("1"),
                Integer.parseInt("2"));
    }
}
