package jd.core.test;

public class AssignInSwitch {

    int assignInSwitch(String s, char c) {
        String line;
        switch ((line = s + ':' + c).charAt(0)) {
            case '0':
                return line.charAt(0);
            default:
                return 1;
        }
    }

    int assignInIf(String s, char c) {
        String line;
        if ((line = s + ':' + c).charAt(0) == '0') {
            return line.charAt(0);
        }
        return 1;
    }
}
