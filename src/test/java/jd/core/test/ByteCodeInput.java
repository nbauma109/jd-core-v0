package jd.core.test;

import java.util.List;

public class ByteCodeInput {

    Object[][] array;
    List<?> list;

    public void invokeInterfaceIInc() {
        for (Object element : list) {
            System.out.println(element);
         }
    }

    public void multiANewArray() {
        array = new Object[1][2];
    }
}
