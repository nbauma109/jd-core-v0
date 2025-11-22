package jd.core.test;

import java.io.ObjectInputStream;
import java.util.List;

public class AssignmentInSynchronized {

    List<?> list;

    int getSize() {
        synchronized (list) {
            return list.size();
        }
    }

    void assignInSynchronized(ObjectInputStream is) throws Exception {
        Object o = null;
        // Object o; // TODO FIXME
        synchronized (o = is.readObject()) {
            System.out.println(o);
        }
    }
}
