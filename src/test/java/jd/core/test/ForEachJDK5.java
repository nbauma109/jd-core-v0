package jd.core.test;

import java.util.List;

public class ForEachJDK5 {

    static void enhancedForEachStatement(List<String> elements) {
        for (String element : elements) {
            print(element);
        }
    }

    static native void print(String element);

    static public void main(String[] args) {
        for (String arg : args) {
            print(arg);
        }
    }
}
