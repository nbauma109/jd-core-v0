package jd.core.test;

import java.util.List;

public class ByteCodeInput {

    Object[][] array;
    List<?> list;

    public void invokeInterfaceIInc() {
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
         }
    }

    public void multiANewArray() {
        array = new Object[1][2];
    }
}
