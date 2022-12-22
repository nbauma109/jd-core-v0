package jd.core.test;

public class TryFinally {

    int value;

    public void tryFinally(int value) {
        setValue(0);
        try {
            setValue(1);
            try {
                setValue(value);
            } finally {
                setValue(2);
            }
        } finally {
            setValue(3);
        }
    }

    public void tryEmptyCatchFinally(int value) {
        setValue(0);
        try {
            setValue(1);
            try {
                setValue(value);
            } catch (Exception e) {
            } finally {
                setValue(2);
            }
        } catch (Exception e) {
        } finally {
            setValue(3);
        }
    }

    public void trySameFinally(int value) {
        setValue(0);
        try {
            setValue(1);
            try {
                setValue(value);
            } finally {
                setValue(2);
            }
        } finally {
            setValue(2);
        }
    }

    public void tryEmptyCatchSameFinally(int value) {
        setValue(0);
        try {
            setValue(1);
            try {
                setValue(value);
            } catch (Exception e) {
            } finally {
                setValue(2);
            }
        } catch (Exception e) {
        } finally {
            setValue(2);
        }
    }

    public void setValue(int value) {
        this.value = value;
    }
}
