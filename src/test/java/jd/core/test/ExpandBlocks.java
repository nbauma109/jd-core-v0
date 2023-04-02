package jd.core.test;

public class ExpandBlocks {

    private String id;
    static String name;

    private void init()
    {
        id = name = "";
    }
    
    private static void staticInit()
    {
        name = "";
    }
    
    public void method1()
    {
        System.out.println("Test.method1");
    }

    static
    {
        System.out.println("clinit1");
    }

    public void method2()
    {
        System.out.println("Test.method2");
    }

    public ExpandBlocks(String id)
    {
        this.id = id;
    }

    class Inner1
    {

        int x, y;

        public Inner1(int x, int y)
        {
            System.out.println("Inner1 constructor");
            this.x = x;
            this.y = y;
            ExpandBlocks.this.id = "";
            init();
        }

        public void method1()
        {
            System.out.println("Inner1.method1");
        }

        public void method2()
        {
            System.out.println("Inner1.method2");
        }

        @Override
        public String toString()
        {
            return id;
        }
    }

    public void method3()
    {
        System.out.println("Test.method3");
    }

    public void method4()
    {
        System.out.println("Test.method4");
    }

    static class Inner2
    {

        int x, y;

        Inner2(int x, int y)
        {
            this.x = x;
            this.y = y;
            name = "";
            staticInit();
        }
    }

    class Inner3
    {

        public Inner3()
        {
            System.out.println("Inner3 constructor");
        }

        public void method1()
        {
            System.out.println("Inner3.method1");
        }

        public void method2()
        {
            System.out.println("Inner3.method2");
        }
    }

    class Inner4
    {

        public Inner4()
        {
            System.out.println("Inner4 constructor");
        }

        public void method1()
        {
            System.out.println("Inner4.method1");
        }

        public void method2()
        {
            System.out.println("Inner4.method2");
        }
    }

    public void method5()
    {
        System.out.println("Test.method5");
    }

    public void method6()
    {
        System.out.println("Test.method6");
    }

    static String TEST = "TEST";
}
