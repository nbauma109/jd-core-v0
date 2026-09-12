package jd.core.test.sealedcase;

public enum ImplicitlySealedEnum {
    SPECIAL {
        @Override
        String text() {
            return "special";
        }
    };

    abstract String text();
}
