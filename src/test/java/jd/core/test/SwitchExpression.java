package jd.core.test;

public class SwitchExpression {

    public String simpleSwitch(int i) {
        String s = switch (i) {
        case 0 -> "simpleSwitch case 0";
        case 1 -> "simpleSwitch case 1";
        case 2 -> "simpleSwitch case 2";
        case 3 -> "simpleSwitch case 3";
        default -> throw new IllegalArgumentException();
        };
        return String.format("return from simpleSwitch: %s", s);
    }

    public String returnSwitch(int i) {
        return switch (i) {
        case 0 -> "returnSwitch case 0";
        case 1 -> "returnSwitch case 1";
        case 2 -> "returnSwitch case 2";
        case 3 -> "returnSwitch case 3";
        default -> throw new IllegalArgumentException();
        };
    }
    
    @SuppressWarnings("all")
    public String yieldSwitch(int i) {
        String s = switch (i) {
        case 0 -> {
            System.out.println("yieldSwitch case 0");
            yield "0";
        }
        case 1 -> {
            System.out.println("yieldSwitch case 1");
            yield "1";
        }
        case 2 -> {
            System.out.println("yieldSwitch case 2");
            yield "2";
        }
        case 3 -> {
            System.out.println("yieldSwitch case 3");
            yield "3";
        }
        default -> throw new IllegalArgumentException();
        };
        return String.format("return from yield switch: %s", s);
    }
}
