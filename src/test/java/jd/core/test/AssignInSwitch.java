package jd.core.test;

public class AssignInSwitch {

    int assignInSwitch(String s, char c) {
        String line;
        return switch ((line = s + ':' + c).charAt(0)) {
		case '0' -> line.charAt(0);
		default -> 1;
		};
    }

    int assignInIf(String s, char c) {
        String line;
        if ((line = s + ':' + c).charAt(0) == '0') {
            return line.charAt(0);
        }
        return 1;
    }
}
