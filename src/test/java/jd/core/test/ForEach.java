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

    void forEachWithMethodReference(ForEach f, List<String> elements) {
        elements.forEach(f::print);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    void forEachWithAnonymousClass(List<String> elements) {
        elements.forEach(new Consumer() {
            /*
             * TODO FIXME Anonymous Consumer only works with Object
             */
            @Override
            public void accept(Object o) {
                print(o);
            }
        });
    }

    private static void printStatic(Object o) {
        System.out.println(o);
    }

    private void print(Object o) {
        printStatic(o);
    }
    
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
        }
    }
}
