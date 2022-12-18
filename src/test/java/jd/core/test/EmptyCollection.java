package jd.core.test;

import java.util.Collections;
import java.util.Map;

public class EmptyCollection {
    static final Map<String, String> EMPTY_MAP = Collections.emptyMap();
    static final Map<String, String> SINGLETON_MAP = Collections.singletonMap("", "");
}
