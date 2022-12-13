package jd.core.test;

import java.util.List;
import java.util.function.Consumer;

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

    static void forEachWithMethodReference(ForEach f, List<String> elements) {
        elements.forEach(f::print);
    }

    static void forEachWithAnonymousClass(List<String> elements) {
        elements.forEach(new Consumer<String>() {
            @Override
            public void accept(String x) {
                printStatic(x);
            }
        });
    }

    static void forEachWithLambdaMethod(List<String> elements) {
        elements.forEach(x -> printStatic(x));
    }

    private static void printStatic(String element) {
        System.out.println(element);
    }

    private void print(String element) {
        printStatic(element);
    }
    
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
        }
    }
}
