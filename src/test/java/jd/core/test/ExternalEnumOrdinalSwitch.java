package jd.core.test;

import java.time.DayOfWeek;

public class ExternalEnumOrdinalSwitch {

    int rank(DayOfWeek day) {
        switch (day.ordinal()) {
            case 0:
                return 1;
            case 1:
                return 2;
            default:
                return 0;
        }
    }
}
