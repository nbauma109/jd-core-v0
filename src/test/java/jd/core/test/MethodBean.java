package jd.core.test;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/*
 * Test for CountDuploadVisitor.
 */
public class MethodBean {

    private String methodName;
    
    public MethodBean(String methodName) {
        this.methodName = methodName;
    }

    Method getMatchingMethod(Class<?> cls, String methodName, Class<?>... parameterTypes) {
        Validate.notNull(cls, "cls");
        Validate.notEmpty(methodName, "methodName");

        MethodBean methodBean = new MethodBean(methodName);
        List<Method> methods = Arrays.stream(cls.getDeclaredMethods()).filter(methodBean::matchName).collect(toList());

        ClassUtils.getAllSuperclasses(cls).stream().map(Class::getDeclaredMethods).flatMap(Arrays::stream).filter(methodBean::matchName)
                .forEach(methods::add);

        for (Method method : methods) {
            if (Arrays.deepEquals(method.getParameterTypes(), parameterTypes)) {
                return method;
            }
        }

        throw new IllegalStateException(String.format("Could not find method %s on class %s",
                methodName + Arrays.stream(parameterTypes).map(String::valueOf).collect(Collectors.joining(",", "(", ")")), cls.getName()));
    }

    public boolean matchName(Method method) {
        return method.getName().equals(methodName);
    }
}
