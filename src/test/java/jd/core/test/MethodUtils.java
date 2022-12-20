package jd.core.test;

import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/*
 * Test for CountDuploadVisitor.
 */
public class MethodUtils {

    public static Method getMethod(Class<?> cls,
            Class<?>... parameterTypes) {
        List<Method> methods = Arrays.stream(cls.getDeclaredMethods())
                .collect(toList());

        ClassUtils.getAllSuperclasses(cls).stream()
                .map(Class::getDeclaredMethods)
                .flatMap(Arrays::stream)
                .forEach(methods::add);

        for (Method method : methods) {
            if (Arrays.deepEquals(method.getParameterTypes(), parameterTypes)) {
                return method;
            }
        }
        return null;
    }
}
