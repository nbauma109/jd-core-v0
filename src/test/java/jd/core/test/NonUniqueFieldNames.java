package jd.core.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        long temp = Double.doubleToLongBits(d);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + Float.floatToIntBits(f);
        result = prime * result + (flag ? 1231 : 1237);
        result = prime * result + i;
        result = prime * result + (int) (l ^ (l >>> 32));
        result = prime * result + ((m == null) ? 0 : m.hashCode());
        result = prime * result + s;
        result = prime * result + ((str == null) ? 0 : str.hashCode());
        result = prime * result + ((t == null) ? 0 : t.hashCode());
        return prime * result + ((w == null) ? 0 : w.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
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
        if (!Objects.equals(m, other.m)) {
            return false;
        }
        if (s != other.s) {
            return false;
        }
        if (!Objects.equals(str, other.str)) {
            return false;
        }
        if (!Objects.equals(t, other.t)) {
            return false;
        }
        if (!Objects.equals(w, other.w)) {
            return false;
        }
        return true;
    }

}
