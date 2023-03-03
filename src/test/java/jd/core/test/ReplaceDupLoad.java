package jd.core.test;

import org.apache.commons.lang3.StringUtils;

import h.ST_pointnlink_t;
import h.ST_triangle_t;
import smetana.core.CArray;
import smetana.core.Globals;
import smetana.core.__ptr__;

public abstract class ReplaceDupLoad {

    /*
     * COMPLEXIF
     */
    public static String abbreviateMiddle(String str, String middle, int length) {
        if (StringUtils.isAnyEmpty(str, middle) || length >= str.length() || length < middle.length() + 2) {
            return str;
        }

        int targetSting = length - middle.length();
        int startOffset = targetSting / 2 + targetSting % 2;
        int endOffset = str.length() - targetSting / 2;

        return str.substring(0, startOffset) + middle + str.substring(endOffset);
    }

    /*
     * FOR
     */
    public static void loadtriangle(__ptr__ pnlap, __ptr__ pnlbp, __ptr__ pnlcp) {
        CArray<ST_triangle_t> trip;
        trip = Z.z().tris.plus_(Z.z().tril++);
        trip.get__(0).mark = 0;
        trip.get__(0).e[0].pnl0p = (ST_pointnlink_t) pnlap;
        trip.get__(0).e[0].pnl1p = (ST_pointnlink_t) pnlbp;
        trip.get__(0).e[0].rtp = null;
        trip.get__(0).e[1].pnl0p = (ST_pointnlink_t) pnlbp;
        trip.get__(0).e[1].pnl1p = (ST_pointnlink_t) pnlcp;
        trip.get__(0).e[1].rtp = null;
        trip.get__(0).e[2].pnl0p = (ST_pointnlink_t) pnlcp;
        trip.get__(0).e[2].pnl1p = (ST_pointnlink_t) pnlap;
        trip.get__(0).e[2].rtp = null;
        for (int ei = 0; ei < 3; ei++) {
            trip.get__(0).e[ei].lrp = trip;
        }
    }

    class Z {
        native static Globals z();
    }
}
