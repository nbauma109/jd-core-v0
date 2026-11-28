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

    static String range(int k) {
        return String.valueOf(work(k), 0, 1) + k;
    }
}
