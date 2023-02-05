package jd.core.test;

class RangeTrie {

    class RangeEntryMap<K> {

        K fromKey;
        K toKey;
        boolean fromInclusive;
        boolean toInclusive;

        RangeEntryMap(K fromKey, K toKey) {
            this(fromKey, true, toKey, false);
        }

        RangeEntryMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            this.fromKey = fromKey;
            this.fromInclusive = fromInclusive;
            this.toKey = toKey;
            this.toInclusive = toInclusive;
        }
    }

    class StringRangeEntryMap {

        String fromKey;
        String toKey;
        boolean fromInclusive;
        boolean toInclusive;

        StringRangeEntryMap(String fromKey, String toKey) {
            this(fromKey, true, toKey, false);
        }

        StringRangeEntryMap(String fromKey, boolean fromInclusive, String toKey, boolean toInclusive) {
            this.fromKey = fromKey;
            this.fromInclusive = fromInclusive;
            this.toKey = toKey;
            this.toInclusive = toInclusive;
        }
    }
}
