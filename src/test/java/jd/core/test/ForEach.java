package jd.core.test;

import java.util.List;

public class ForEach {

    void enhancedForEachStatement(List<String> elements) {
        for (String element : elements) {
            print(element);
        }
    }

    void forEachWithMethodReference(List<String> elements) {
        elements.forEach(ForEach::print);
    }

    private static void print(String element) {
        System.out.println(element);
    }
}
