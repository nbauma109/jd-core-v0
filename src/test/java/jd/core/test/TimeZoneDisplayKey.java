package jd.core.test;

import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class TimeZoneDisplayKey {

    private final TimeZone mTimeZone;
    private final int mStyle;
    private final Locale mLocale;

    public TimeZoneDisplayKey(TimeZone mTimeZone, int mStyle, Locale mLocale) {
        this.mTimeZone = mTimeZone;
        this.mStyle = mStyle;
        this.mLocale = mLocale;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mLocale, mStyle, mTimeZone);
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
        TimeZoneDisplayKey other = (TimeZoneDisplayKey) obj;
        return
                Objects.equals(mLocale, other.mLocale) &&
                mStyle == other.mStyle &&
                Objects.equals(mTimeZone, other.mTimeZone);
    }
}
