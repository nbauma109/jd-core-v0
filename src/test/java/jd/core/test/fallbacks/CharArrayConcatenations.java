package jd.core.test.fallbacks;

/** Concatenations of the content of character arrays. */
// Explicit builders and conversions are the shapes under test
@SuppressWarnings({ "java:S1153", "java:S1488", "java:S1643", "java:S3457" })
public class CharArrayConcatenations {
    static int failures;
    static StringBuilder log = new StringBuilder();

    static char[] work(int k) {
        return new char[] { 'a', (char) ('b' + k) };
    }

    static String concat(int k) {
        return String.valueOf(work(k)) + k;
    }

    static String surrounded(int k) {
        return "<" + String.valueOf(work(k)) + ">";
    }

    static String local(int k) {
        char[] chars = work(k);
        String s = String.valueOf(chars) + chars.length;
        return s;
    }

    static String builder(int k) {
        return new StringBuilder().append(work(k)).append(k).toString();
    }

    static String appendAfterString(int k) {
        return new StringBuilder().append("x").append(work(k)).toString();
    }

    static String builderOfString(int k) {
        return new StringBuilder(String.valueOf(work(k))).append("y").append(k).toString();
    }

    static String others(int k) {
        Object o = k > 0 ? "o" : null;
        return String.valueOf(o) + String.valueOf(k) + String.valueOf(k > 0);
    }

    static String builderOfNumber(int k) {
        return new StringBuilder(String.valueOf(k)).append("z").toString();
    }

    static String builderOfRange(int k) {
        return new StringBuilder(String.valueOf(work(k), 0, 1)).append("w").toString();
    }

    static String copied(int k) {
        return String.copyValueOf(work(k)) + k;
    }

    static String leadingNumbers(int i, int j) {
        return "" + i + j + String.valueOf(work(i));
    }

    static String leadingNumbersBeforeLiteral(int i, int j) {
        return "" + i + j + "v";
    }

    static String builderOfNumbers(int i, int j) {
        return new StringBuilder().append(i).append(j).append("u").toString();
    }

    static String builderOfCharacter(int i) {
        char c = 'c';
        return new StringBuilder().append(c).append(i).append("t").toString();
    }

    static String secondOperandString(int i) {
        return new StringBuilder().append(i).append("s").append(i).toString();
    }

    static String sum(int i, int j) {
        return (i + j) + "r";
    }

    static String range(int k) {
        return String.valueOf(work(k), 0, 1) + k;
    }
}
