package jd.core.test;

public enum FruitWithName implements Eatable, Disposable {
    APPLE("apple"), BANANA("banana"), KIWI("kiwi");
    
    String name;

    private FruitWithName(String name) {
        this.name = name;
    }
}
