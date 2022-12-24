package jd.core.test;

import java.util.List;

public class ByteCodeInput {

    private Object[][] array;

    public void invokeInterfaceIInc(List<?> list) {
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
         }
    }

    public void multiANewArray() {
        array = new Object[1][2];
    }
}
