package jd.core.test;

import java.awt.Component;
import java.awt.Point;
import java.lang.reflect.Constructor;

public class AddCheckCast {

    ReflectionData reflectionData;
    static StaticReflectionData staticReflectionData;

    int getField(Component c) {
        Point p = c != null ? c.getLocation() : null;
        return p.x;
    }

    void putField(Component c) {
        int i = c != null ? c.getWidth() : 0;
        Point p = c != null ? c.getLocation() : null;
        p.x = i;
    }

    Constructor<?>[] privateGetDeclaredConstructors(Class<?> c, boolean publicOnly) {
        Constructor<?>[] res;
        ReflectionData rd = reflectionData();
        if (rd != null) {
            res = publicOnly ? rd.publicConstructors : rd.declaredConstructors;
            if (res != null)
                return res;
        }
        if (c.isInterface()) {
            Constructor<?>[] temporaryRes = new Constructor[0];
            res = temporaryRes;
        } else {
            res = c.getDeclaredConstructors();
        }
        if (rd != null) {
            if (publicOnly) {
                rd.publicConstructors = res;
            } else {
                rd.declaredConstructors = res;
            }
        }
        return res;
    }

    @SuppressWarnings("static-access")
    Constructor<?>[] staticPrivateGetDeclaredConstructors(Class<?> c, boolean publicOnly) {
        Constructor<?>[] res;
        StaticReflectionData rd = staticReflectionData();
        if (rd != null) {
            res = publicOnly ? rd.publicConstructors : rd.declaredConstructors;
            if (res != null)
                return res;
        }
        if (c.isInterface()) {
            Constructor<?>[] temporaryRes = new Constructor[0];
            res = temporaryRes;
        } else {
            res = c.getDeclaredConstructors();
        }
        if (rd != null) {
            if (publicOnly) {
                rd.publicConstructors = res;
            } else {
                rd.declaredConstructors = res;
            }
        }
        return res;
    }

    ReflectionData reflectionData() {
        return reflectionData;
    }

    StaticReflectionData staticReflectionData() {
        return staticReflectionData;
    }

    class ReflectionData {
        Constructor<?>[] publicConstructors;
        Constructor<?>[] declaredConstructors;
    }

    static class StaticReflectionData {
        static Constructor<?>[] publicConstructors;
        static Constructor<?>[] declaredConstructors;
    }
}
