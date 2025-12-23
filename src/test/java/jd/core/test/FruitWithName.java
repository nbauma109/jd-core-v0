package jd.core.test;

public enum FruitWithName implements Eatable, Disposable {
    APPLE("apple"), BANANA("banana"), KIWI("kiwi");

    String name;

    FruitWithName(String name) {
        this.name = name;
    }
}
