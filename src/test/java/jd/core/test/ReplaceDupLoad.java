package jd.core.test;

import org.apache.commons.lang3.StringUtils;

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

}
