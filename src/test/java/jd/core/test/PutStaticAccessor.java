package jd.core.test;

public class PutStaticAccessor {

    private static Object theObject = null;

    static void init() {
        new Thread(() -> {
		    if (theObject == null) {
		        theObject = new Object();
		    }
		});
    }
}
