package jd.core.test;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

public class TypeArgument {

    void sort1(List<Entry<int[], List<? super Object>>> list) {
        list.sort(new Comparator<Entry<int[], List<? super Object>>>() {

            @Override
            public int compare(Entry<int[], List<? super Object>> o1, Entry<int[], List<? super Object>> o2) {
                return 0;
            }
        });
    }

    void sort2(List<Entry<byte[], List<? extends Object>>> list) {
        list.sort(new Comparator<Entry<byte[], List<? extends Object>>>() {

            @Override
            public int compare(Entry<byte[], List<? extends Object>> o1, Entry<byte[], List<? extends Object>> o2) {
                return 0;
            }
        });
    }

    void sort3(List<Entry<float[], List<?>[][]>> list) {
        list.sort(new Comparator<Entry<float[], List<?>[][]>>() {

            @Override
            public int compare(Entry<float[], List<?>[][]> o1, Entry<float[], List<?>[][]> o2) {
                return 0;
            }
        });
    }

    <T extends Serializable & Comparable<T>> void sort4(List<Entry<double[], List<T>>[][]> list) {
        list.sort(new Comparator<Entry<double[], List<T>>[][]>() {

            @Override
            public int compare(Entry<double[], List<T>>[][] o1, Entry<double[], List<T>>[][] o2) {
                return 0;
            }
        });
    }
}
