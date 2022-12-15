package jd.core.test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/*
 * Memo Jikes
 * jikes -g -bootclasspath C:\j2sdk1.4.2_19\jre\lib\rt.jar src\test\java\jd\core\test\MonitorSynchronized.java -d target\test-classes
 * jar cvf src\test\resources\monitor-synchronized-jikes-1.22.jar -C target\test-classes jd\core\test\MonitorSynchronized.class
 */
public class MonitorSynchronized {

    private volatile Object resource;

    /*
     * Example from Sonarsource
     * https://rules.sonarsource.com/java/RSPEC-2168
     */
    public Object getResource() {
      Object localResource = resource;
      if (localResource == null) {
        synchronized (this) {
          localResource = resource;
          if (localResource == null) {
            resource = localResource = new Object();
          }
        }
      }
      return localResource;
    }

    /*
     * Example from javadoc
     * https://docs.oracle.com/javase/7/docs/api/java/util/Collections.html#synchronizedMap(java.util.Map)
     */
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
