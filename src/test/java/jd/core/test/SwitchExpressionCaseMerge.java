package jd.core.test;

public class SwitchExpressionCaseMerge {

    public int withSideEffect(int i) {
        return switch (i) {
            case 1, 2 -> {
                System.out.println("hit");
                yield 42;
            }
            default -> 0;
        };
    }
}
