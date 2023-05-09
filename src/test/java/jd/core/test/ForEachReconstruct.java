package jd.core.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ForEachReconstruct<T> {

    private boolean flag;
    private List<T> _objects;

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
    
    void enhancedForEachStatement(Long... longs) {
        for (Long aLong : longs) {
            System.out.println(aLong);
        }
        List<Long> longList = new ArrayList<>();
        if (longs != null) {
            for (long theLong : longs) {
                longList.add(theLong);
            }
        }
    }
    
    void enhancedForEachStatement(Double... doubles) {
        for (Double aDouble : doubles) {
            System.out.println(aDouble);
        }
        List<Double> doubleList = new ArrayList<>();
        if (doubles != null) {
            for (double theDouble : doubles) {
                doubleList.add(theDouble);
            }
        }
    }
    
    void enhancedForEachStatement(Float... floats) {
        for (Float aFloat : floats) {
            System.out.println(aFloat);
        }
        List<Float> floatList = new ArrayList<>();
        if (floats != null) {
            for (float theFloat : floats) {
                floatList.add(theFloat);
            }
        }
    }
    
    void enhancedForEachStatement(Byte... bytes) {
        for (Byte aByte : bytes) {
            System.out.println(aByte);
        }
        List<Byte> byteList = new ArrayList<>();
        if (bytes != null) {
            for (byte theByte : bytes) {
                byteList.add(theByte);
            }
        }
    }
    
    void enhancedForEachStatement(Short... shorts) {
        for (Short aShort : shorts) {
            System.out.println(aShort);
        }
        List<Short> shortList = new ArrayList<>();
        if (shorts != null) {
            for (short theShort : shorts) {
                shortList.add(theShort);
            }
        }
    }
    
    void enhancedForEachStatement(Character... chars) {
        for (Character aChar : chars) {
            System.out.println(aChar);
        }
        List<Character> charList = new ArrayList<>();
        if (chars != null) {
            for (char theChar : chars) {
                charList.add(theChar);
            }
        }
    }
    
    void enhancedForEachStatement(Boolean... booleans) {
        for (Boolean aBoolean : booleans) {
            System.out.println(aBoolean);
        }
        List<Boolean> booleanList = new ArrayList<>();
        if (booleans != null) {
            for (boolean theBoolean : booleans) {
                booleanList.add(theBoolean);
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

    /*
     * CHECKCAST in foreach array pattern
     */
    public Map<T, String> addItems(T[] objects, T beforeObject) {
        HashMap<T, String> messages = null;

        if (!this.flag) {
            for (T object : objects)
                this._objects.remove(object);
        }
        int at = -1;

        if (beforeObject != null) {
            at = this._objects.indexOf(beforeObject);
        }
        if (at >= 0) {
            for (T object : objects) {
                String message = object.toString();

                if (message == null) {
                    this._objects.add(at++, object);
                } else {
                    if (messages == null)
                        messages = new HashMap<>();
                    messages.put(object, message);
                }
            }
        } else {
            for (T object : objects) { // Generates CHECKCAST in JDK8
                String message = object.toString();

                if (message == null) {
                    this._objects.add(object);
                } else {
                    if (messages == null)
                        messages = new HashMap<>();
                    messages.put(object, message);
                }
            }
        }

        return messages;
    }
}
