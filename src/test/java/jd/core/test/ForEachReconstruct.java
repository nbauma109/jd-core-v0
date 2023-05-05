package jd.core.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ForEachReconstruct {

    /*
     * SUN 1.5 pattern
     */
    void forEachInCatch() {
        try {
            System.out.println("try");
        } catch (Exception e) {
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                System.out.println(stackTraceElement);
            }
        }
    }

    /*
     * IBM pattern
     */
    public static void main(String[] args) {
        Object[] localObject = args; int guiMap = 0; for (int localGUIMap1 = localObject.length; guiMap < localGUIMap1; ++guiMap) { Object arg = localObject[guiMap];
            System.out.println(arg);
        }
    }

    /*
     * Foreach with auto-unboxing
     */
    void enhancedForEachStatement(Integer... ints) {
        for (Integer anInt : ints) {
            System.out.println(anInt);
        }
        List<Integer> intList = new ArrayList<>();
        if (ints != null) {
            for (int theInt : ints) {
                intList.add(theInt);
            }
        }
    }

    /*
     * Empty foreach
     */
    void emptyForEach(Class c) {
        Method[] methods = c.getMethods();
        Map m = new HashMap();
        for (Method method : methods) {
            StringBuilder sb = new StringBuilder();
            sb.append(method.getName());
            if (method.getReturnType().equals(void.class)) {
                sb.append("");
            }
            m.put(method.getName(), sb.toString());
        }
        for (Iterator iter = m.values().iterator(); iter.hasNext(););
    }
}
