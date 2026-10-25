package jd.core.test;

public class NonEnumOrdinalSwitch {

    static class Rank {
        int ordinal() {
            return 1;
        }
    }

    int describe(Rank rank) {
        switch (rank.ordinal()) {
            case 0:
                return 10;
            case 1:
                return 20;
            default:
                return 0;
        }
    }
}
