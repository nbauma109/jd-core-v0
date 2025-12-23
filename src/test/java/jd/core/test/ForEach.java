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

    void forEachWithAnonymousClass(List<String> elements) {
        elements.forEach(this::print);
    }

    private static void printStatic(Object o) {
        System.out.println(o);
    }

    private void print(Object o) {
        printStatic(o);
    }

    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println(arg);
        }
    }
}
