package jd.core.test;

public class InitArray {

    String[] s1 = Math.random() < 0.5 ? new String[] { "a", "b", "c" } : new String[] { "d" };
    String[] s2 = Math.random() < 0.5 ? new String[] { "a", "b", "c" } : new String[] { "d", "e" };
    String[] s3 = Math.random() < 0.5 ? new String[] { "a", "b", "c" } : new String[] { "d", "e", "f" };
}
