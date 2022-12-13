package jd.core.test;

public class SimpleThrowableClass extends Throwable {

    private static final long serialVersionUID = 1L;

    public SimpleThrowableClass() {
        super("Hello World");
    }
}
