package jd.core.test;
/*
 * Test input built on top of example from Procyon's wiki page:
 * https://github.com/mstrobel/procyon/wiki/Decompiler-Output-Comparison.
 */
public class ArrayCreation {

    static final int[] table1 = new int[126];
    static final int[] table2 = new int[127];
    static final int[] table3 = new int[128];
    static final int[] table4 = new int[32766];
    static final int[] table5 = new int[32767];
    static final int[] table6 = new int[32768];
    static final int[] table7 = new int[65534];
    static final int[] table8 = new int[65535];
    static final int[] table9 = new int[65536];
    static final int[] table10 = new int[2147483646];
    static final int[] table11 = new int[2147483647];
    static final int[] table12 = new int[Integer.MAX_VALUE+1];

    static final boolean[] booleans = { true, false };
    static final short[] shorts = { 0, 1, 2, 3 };
    static final byte[] bytes = { 0, 1, 2, 3 };

    public int[] arrayCreation(final boolean initialize) {
        if (initialize) {
            return new int[] { 1, 2, 3 };
        }
        return new int[3];
    }

    public int[][] jaggedArrayInitialization() {
        return new int[][] { { 1, 2, 3 }, { 4, 5, 6 } };
    }

    public int[][] multiDimensionalArrayCreation() {
        return new int[3][2];
    }
}
