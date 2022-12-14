package jd.core.test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MonitorSynchronized {

    void navigate() {
        Map m = Collections.synchronizedMap(new HashMap());
        Set s = m.keySet(); // Needn't be in synchronized block
        synchronized (m) { // Synchronizing on m, not s!
            Iterator i = s.iterator(); // Must be in synchronized block
            while (i.hasNext()) {
                System.out.println(i.next());
            }
        }
    }
}
