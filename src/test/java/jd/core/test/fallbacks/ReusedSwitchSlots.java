package jd.core.test.fallbacks;

import java.util.List;

/** The temporary variables of the switch reuse the slots of the loop variables, which have no entry of their own. */
public class ReusedSwitchSlots {
    @SuppressWarnings("java:S1488") // The variable keeps the slot layout under test
    static String check(StringBuilder log, List<String> entries) {
        try {
            String name = entries.get(0);
            Class<?> type = Class.forName(name);
            log.append(name).append(type);
            for (String entry : entries) {
                log.append(entry);
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        String first = entries.get(0);
        String kind = switch (first) {
            case "RSA", "DSA" -> first;
            default -> "UNKNOWN";
        };
        return kind;
    }
}
