package jd.core.test;

public class StringConcat {

    String concat(String a, String b, String c) {
        return a + ":" + b + ":" + c;
    }

    public String needsLeadingEmptyString(int i) {
        return "" + i;
    }

    public String usesBootstrapConstant2(String s) {
        return "\u0001" + s;
    }
}
