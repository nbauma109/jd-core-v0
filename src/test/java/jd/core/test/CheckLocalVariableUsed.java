package jd.core.test;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class CheckLocalVariableUsed {

    int testUnaryOpArrayLength() {
        try {
            throw new Exception();
        } catch (Exception e) {
            return -e.getStackTrace().length;
        }
    }

    void testBinaryOpInvokeNew() {
        try {
            System.out.println("try");
        } catch (Exception e) {
            throw new Error("Error " + e.getMessage());
        }
    }

    boolean testInstanceOf() {
        try {
            throw new Exception();
        } catch (Exception e) {
            return e instanceof RuntimeException;
        }
    }

    void testCheckcast() {
        try {
            System.out.println("try");
        } catch (Exception e) {
            throw (RuntimeException) e;
        }
    }

    int[] newArray() {
        try {
            System.out.println("try");
        } catch (Exception e) {
            return new int[e.getStackTrace().length];
        }
        return null;
    }

    Integer[] aNewArray() {
        try {
            System.out.println("try");
        } catch (Exception e) {
            return new Integer[e.getStackTrace().length];
        }
        return null;
    }

    Integer[][] multiNewArray() {
        try {
            System.out.println("try");
        } catch (Exception e) {
            return new Integer[e.getStackTrace().length][e.getStackTrace().length];
        }
        return null;
    }

    String[] invokeStatic() {
        try {
            System.out.println("try");
        } catch (Exception e) {
            return ExceptionUtils.getRootCauseStackTrace(e);
        }
        return null;
    }

    StackTraceElement arrayLoadAssignment() {
        try {
            System.out.println("try");
        } catch (Exception e) {
            return e.getStackTrace()[0] = null;
        }
        return null;
    }
}
