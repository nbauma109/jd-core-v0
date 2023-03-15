package jd.core.test;

import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ContinueInCatch {

    public static Method getPublicMethod(Class<?> cls, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {

        Method declaredMethod = cls.getMethod(methodName, parameterTypes);
        if (Modifier.isPublic(declaredMethod.getDeclaringClass().getModifiers())) {
            return declaredMethod;
        }

        List<Class<?>> candidateClasses = new ArrayList<>(ClassUtils.getAllInterfaces(cls));
        candidateClasses.addAll(ClassUtils.getAllSuperclasses(cls));

        for (Class<?> candidateClass : candidateClasses) {
            if (!Modifier.isPublic(candidateClass.getModifiers())) {
                continue;
            }
            Method candidateMethod = null;
            try {
                candidateMethod = candidateClass.getMethod(methodName, parameterTypes);
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            } catch (NoSuchMethodException ex) {
                continue;
            }
            if (Modifier.isPublic(candidateMethod.getDeclaringClass().getModifiers())) {
                return candidateMethod;
            }
        }

        throw new NoSuchMethodException();
    }
}
