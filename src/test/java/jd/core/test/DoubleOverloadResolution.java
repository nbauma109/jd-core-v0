package jd.core.test;

/**
 * Test class demonstrating why double constants must preserve 'D' suffix.
 * If a CONSTANT_Double is decompiled as a float literal, overload resolution changes.
 */
public class DoubleOverloadResolution {

    // Method overloads to test resolution
    public static String testMethod(float f) {
        return "float";
    }

    public static String testMethod(double d) {
        return "double";
    }

    // When bytecode contains: testMethod((double)0.2F)
    // It stores CONSTANT_Double and calls testMethod(double)
    // If decompiled as: testMethod(0.2F), it would call testMethod(float)
    public static void demonstrateIssue() {
        // These should all call the double overload when decompiled
        String result1 = testMethod(1.0D);
        String result2 = testMethod(0.5D);
        String result3 = testMethod(2.0D);

        System.out.println(result1);
        System.out.println(result2);
        System.out.println(result3);
    }
}
