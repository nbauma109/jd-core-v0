package jd.core.test;

import org.apache.commons.lang3.Validate;

public class VarArgsFalsePositives extends AbstractTestCase {

    public static void main(String[] args) {
        Validate.noNullElements(args, "error", new Object[1]);
        Validate.noNullElements(args, "error", new Object[args.length]);
    }
}
