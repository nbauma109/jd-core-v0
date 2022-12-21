package jd.core.test;

public class LabelledBreaks {


    public void tripleWhile2(int i) {

        System.out.println("start");

        while (i > 1) {
            label: while (i > 2) {
                while (i > 3) {
                    System.out.println("a");

                    if (i == 4) {
                        continue label;
                    }
                    if (i == 5) {
                        break label;
                    }

                    System.out.println("b");
                }
            }
        }

        System.out.println("end");
    }
}