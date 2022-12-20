package jd.core.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class NonUniqueFieldNames<T> {

    String str;
    byte b;
    boolean flag;
    short s;
    char c;
    int i;
    long l;
    float f;
    double d;
    byte b1d[];
    byte b2d[][];
    List<?> w;
    List<T> t;
    Map<? extends T, ? super T> m;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + b;
        result = prime * result + Arrays.hashCode(b1d);
        result = prime * result + Arrays.deepHashCode(b2d);
        result = prime * result + c;
        long temp;
        temp = Double.doubleToLongBits(d);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + Float.floatToIntBits(f);
        result = prime * result + (flag ? 1231 : 1237);
        result = prime * result + i;
        result = prime * result + (int) (l ^ (l >>> 32));
        result = prime * result + ((m == null) ? 0 : m.hashCode());
        result = prime * result + s;
        result = prime * result + ((str == null) ? 0 : str.hashCode());
        result = prime * result + ((t == null) ? 0 : t.hashCode());
        result = prime * result + ((w == null) ? 0 : w.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        NonUniqueFieldNames other = (NonUniqueFieldNames) obj;
        if (b != other.b) {
            return false;
        }
        if (!Arrays.equals(b1d, other.b1d)) {
            return false;
        }
        if (!Arrays.deepEquals(b2d, other.b2d)) {
            return false;
        }
        if (c != other.c) {
            return false;
        }
        if (Double.doubleToLongBits(d) != Double.doubleToLongBits(other.d)) {
            return false;
        }
        if (Float.floatToIntBits(f) != Float.floatToIntBits(other.f)) {
            return false;
        }
        if (flag != other.flag) {
            return false;
        }
        if (i != other.i) {
            return false;
        }
        if (l != other.l) {
            return false;
        }
        if (m == null) {
            if (other.m != null) {
                return false;
            }
        } else if (!m.equals(other.m)) {
            return false;
        }
        if (s != other.s) {
            return false;
        }
        if (str == null) {
            if (other.str != null) {
                return false;
            }
        } else if (!str.equals(other.str)) {
            return false;
        }
        if (t == null) {
            if (other.t != null) {
                return false;
            }
        } else if (!t.equals(other.t)) {
            return false;
        }
        if (w == null) {
            if (other.w != null) {
                return false;
            }
        } else if (!w.equals(other.w)) {
            return false;
        }
        return true;
    }

}
