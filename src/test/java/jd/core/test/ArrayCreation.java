package jd.core.test;
/*
 * Test input built from Procyon's wiki page:
 * https://github.com/mstrobel/procyon/wiki/Decompiler-Output-Comparison.
 */
public class ArrayCreation {
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
