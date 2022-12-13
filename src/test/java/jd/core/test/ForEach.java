package jd.core.test;

import java.util.List;

public class ForEach {

    void enhancedForEachStatement(List<String> elements) {
        for (String element : elements) {
            print(element);
        }
    }

    void forEachWithStaticMethodReference(List<String> elements) {
        elements.forEach(ForEach::printStatic);
    }

    void forEachWithMethodReference(List<String> elements) {
        elements.forEach(this::print);
    }

    void forEachWithMethodReference(ForEach f, List<String> elements) {
        elements.forEach(f::print);
    }

    void forEachWithLambdaMethod(List<String> elements) {
        elements.forEach(x -> print(x));
    }

    private static void printStatic(String element) {
        System.out.println(element);
    }

    private void print(String element) {
        printStatic(element);
    }
}
