package jd.core.test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SimpleClass {

    void print(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }
    }

    void printMap(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
    }

    void printIter(List<String> list) {
        for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
            System.out.println(iterator.next());
        }
    }

    void printJDK5(List<String> list) {
        for (String elem : list) {
            System.out.println(elem);
        }
    }

    void printJDK8(List<String> list) {
        list.forEach(System.out::println);
    }
}
