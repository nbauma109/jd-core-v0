package jd.core.test;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

public class BreakContinue {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    Collection process(Collection collection) {
        Collection result = new LinkedList<>();
        for (Object o : collection) {
            if (!result.isEmpty()) {
                break;
            }
            if (Objects.isNull(o) || !(o instanceof Collection)) {
                continue;
            }
            Collection<?> c = (Collection<?>) o;
            if (Objects.isNull(c) || c.isEmpty()) {
                continue;
            }
            Iterator<?> iterator = c.iterator();
            if (!iterator.hasNext()) {
                continue;
            }
            Object next = iterator.next();
            if (next != null) {
                result.add(next);
                break;
            }
        }
        return result;
    }
}
