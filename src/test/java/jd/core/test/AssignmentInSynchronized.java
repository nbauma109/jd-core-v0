package jd.core.test;

import java.util.List;

public class AssignmentInSynchronized {

    List<?> list;

    int getSize() {
        synchronized (list) {
            return list.size();
        }
    }
}
