package jd.core.test;

@SuppressWarnings("java:S131") public class LoopConditionLineNumbers {
    LoopConditionLineNumbers parent;
    int kind;

    LoopConditionLineNumbers enclosing() {
        LoopConditionLineNumbers current = this;
        while ((current = current.parent) != null) {
            switch (current.kind) {
            case 1:
                return current;
            case 2:
                return current.parent;
            }
        }
        return null;
    }

    static int after() {
        return 42;
    }
}
