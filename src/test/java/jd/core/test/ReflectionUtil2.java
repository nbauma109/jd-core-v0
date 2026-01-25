package jd.core.test;

import java.lang.reflect.Method;

public class ReflectionUtil2 {

    <T> T invokeMethod(Class<?> clazz, Object object, String methodName, Class<?>[] parameterTypes, Object[] parameters) {
        T value = null;
        if (clazz != null && object != null) {
            Method method = null;
            Class<?> superClass = clazz;
            while (superClass != null && superClass != Object.class && method == null) {
                try {
                    method = superClass.getMethod(methodName, parameterTypes);
                } catch (Exception e) {
                    e.printStackTrace();
                    superClass = superClass.getSuperclass();
                    continue;
                }
                break;
            }
            if (method != null) {
                try {
                    value = (T) method.invoke(object, parameters);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }
}
