package jd.core.test;

public class StrBuilder {

    public StrBuilder deleteAll(char ch, int size, char[] buffer) {
        for (int i = 0; i < size; i++) {
            if (buffer[i] == ch) {
                int start = i;
                while (++i < size) {
                    if (buffer[i] != ch) {
                        break;
                    }
                }
                int len = i - start;
                deleteImpl(start, i, len);
                i -= len;
            }
        }
        return this;
    }

    private native void deleteImpl(int start, int i, int len);
}
