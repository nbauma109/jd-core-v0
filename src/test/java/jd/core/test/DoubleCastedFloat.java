package jd.core.test;

@SuppressWarnings("all")
public class DoubleCastedFloat {
    public void test() {
        double x = 0.2F;
    }

    public void testMultiple() {
        double a = 0.1F;
        double b = 0.3F;
        double c = 0.7F;
        double d = 3.14F;
    }

    public void testDirectDouble() {
        double x = 0.2;
    }

    public void testExactValues() {
        // These values can be exactly represented in both float and double
        // so there's no way to tell at bytecode level if they were originally float or double
        double a = 0.5;
        double b = 1.5;
    }
}
