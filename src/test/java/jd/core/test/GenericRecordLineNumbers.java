package jd.core.test;

import java.util.List;
import java.util.Map;

public class GenericRecordLineNumbers {
    private record Cache(List<String> values,
                         Map<String, Integer> index) {
        int count() {
            return values.size();
        }
    }

    static int after() {
        return 42;
    }

    private record VisibleConstructorCache(List<String> values) {
        @SuppressWarnings("java:S1186") public VisibleConstructorCache {
        }
    }

    private record CustomAccessorCache(List<String> values) {
        public List<String> values() {
            return List.copyOf(values);
        }
    }
}
