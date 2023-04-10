package jd.core.test;

public class AssertAssignment {

    void test1(boolean flag) {
        String line;
        assert flag : line = "";
    }
    
    void test2(boolean flag) {
        String line;
        assert (line = flag + "") != null : "";
    }
}
