package jd.core.test;

import java.util.List;

public class ToArray {

    String[] toArray(List<String> list) {
        return list.toArray(String[]::new);
    }
}
