package jd.core.test;

import java.util.List;

public class ForEachWithLambdaMethod {

    void forEachWithLambdaMethod(List<String> elements) {
        elements.stream().sorted((a, b) -> a.compareTo(b)).forEach(o -> System.out.print(o));
    }

    void forEachWithLambdaMethodEmpty(List<String> elements) {
        elements.stream().sorted((a, b) -> a.compareTo(b)).forEach(o -> {});
    }

    void forEachWithLambdaMethodVerbose(List<String> elements) {
        elements.stream().sorted((a, b) -> {
            System.out.println("comparing");
            return a.compareTo(b);
        }).forEach(o -> {
            System.out.print("printing");
            System.out.print(o);
        });
    }
}
