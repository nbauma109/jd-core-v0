package jd.core.test.syntheticcases;

public interface InterfaceSwitch {
    enum Kind { FIRST, SECOND }

    default int choose(Kind kind) {
        switch (kind) {
        case FIRST: return 17;
        case SECOND: return 23;
        default: return 0;
        }
    }
}
