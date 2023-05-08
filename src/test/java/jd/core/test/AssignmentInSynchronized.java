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

    void assignInSynchronized(ObjectInputStream ois) throws Exception {
        Object o = null;
        // Object o; // TODO FIXME
        synchronized (o = ois.readObject()) {
            System.out.println(o);
        }
    }
}
