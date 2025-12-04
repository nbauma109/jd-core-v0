package jd.core.test;

import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;

public class TypeArgument {

    void sort1(List<Entry<int[], List<? super Object>>> list) {
        list.sort((o1, o2) -> 0);
    }

    void sort2(List<Entry<byte[], List<? extends Object>>> list) {
        list.sort((o1, o2) -> 0);
    }

    void sort3(List<Entry<float[], List<?>[][]>> list) {
        list.sort((o1, o2) -> 0);
    }

    <T extends Serializable & Comparable<T>> void sort4(List<Entry<double[], List<T>>[][]> list) {
        list.sort((o1, o2) -> 0);
    }
}
