package jd.core.test;

@SuppressWarnings({"java:S1186", "java:S106", "java:S1854", "java:S1481", "java:S125"})
public class VineflowerSamples {

    public double widenedFloatConstant() {
        return 0.2F;
    }

    public String widenedFloatConstantArgument() {
        return String.valueOf(0.2F + 0.0);
    }

    public int localClassWithField() {
        var counter = new Object() {
            int count;

            int next() {
                return ++count;
            }
        };
        return counter.next();
    }

    public void namelessLocalClass() {
        var printer = new Object() {
            void println(String s) {
                System.out.println(s);
            }
        };
        printer.println("goodbye, world!");
    }

    public void localClassOverridingOnly() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                System.out.println("run");
            }
        };
        r.run();
    }

    public enum EnumWithInitializers {
        A {
            {
                System.out.println("A");
            }
        },
        B {
            {
                System.out.println("B");
            }
        }
    }

    public Object anonymousInitializer() {
        return new Object() {
            {
                System.out.println("init");
            }
        };
    }

    public void tryWithResources(java.io.File file) throws java.io.FileNotFoundException {
        try (java.util.Scanner scanner = new java.util.Scanner(file)) {
            scanner.next();
        }
    }

    public void tryWithResourcesAndFinally(java.io.File file) throws java.io.FileNotFoundException {
        try (java.util.Scanner scanner = new java.util.Scanner(file)) {
            scanner.next();
        } finally {
            System.out.println("Hello");
        }
    }

    public void tryWithResourcesAndCatch(java.io.File first, java.io.File second) throws java.io.FileNotFoundException {
        try (java.util.Scanner scanner1 = new java.util.Scanner(first);
                java.util.Scanner scanner2 = new java.util.Scanner(second)) {
            scanner1.next();
            scanner2.next();
        } catch (RuntimeException e) {
            System.out.println("Caught");
        }
    }

    public String tryWithResourcesAndReturn(String name) throws java.io.IOException {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(name))) {
            return reader.readLine();
        }
    }

    public void tryWithExistingResource(AutoCloseable closeable) throws Exception {
        try (closeable) {
            System.out.println("x");
        }
    }
}
