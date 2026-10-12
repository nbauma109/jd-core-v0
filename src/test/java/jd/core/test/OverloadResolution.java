package jd.core.test;

@SuppressWarnings("all")
public class OverloadResolution {
    // This class demonstrates why we must emit double constants with D suffix
    // even when they could be represented as float literals

    public static String test(float x) {
        return "float";
    }

    public static String test(double x) {
        return "double";
    }

    public void testOverloadResolution() {
        // The compiler chooses the most specific overload (float)
        // So this correctly calls test(float) and decompiles as 0.2F
        String result = test(0.2F);

        // The issue occurs when a double variable is assigned from a float literal
        double d = 0.2F;  // This stores 0.20000000298023224D in bytecode
        // When this double constant appears in code, we must emit it with D suffix
        // to preserve its type and avoid changing overload resolution
    }

    public void demonstrateDoubleConstant() {
        // When explicitly cast to double in method argument
        // The bytecode uses ldc2_w (double constant) and calls double overload
        String result = test((double)0.2F);
        // This should decompile with the explicit cast preserved
        // to ensure it still calls the double overload
    }
}
