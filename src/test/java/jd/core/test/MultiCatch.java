package jd.core.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/*
 * org.apache.commons.lang3.exception.ExceptionUtils.getCauseUsingMethodName(Throwable, String)
 */
public class MultiCatch {
    @SuppressWarnings("unused")
    static Throwable getCauseUsingMethodName(Throwable throwable, String methodName) {
        Method method = null;
        try {
            method = throwable.getClass().getMethod(methodName);
        } catch (NoSuchMethodException | SecurityException ignored) { // NOPMD
            // exception ignored
        }

        if (method != null && Throwable.class.isAssignableFrom(method.getReturnType())) {
            try {
                return (Throwable) method.invoke(throwable);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) { // NOPMD
                // exception ignored
            }
        }
        return null;
    }
}
