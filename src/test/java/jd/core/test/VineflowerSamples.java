package jd.core.test;

@SuppressWarnings({"java:S1186", "java:S106", "java:S1854", "java:S1481", "java:S125"})
public class VineflowerSamples {

    public double widenedFloatConstant() {
        double x = 0.2F;
        return x;
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
}
