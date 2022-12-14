package jd.core.test;

public class IndexOf {

    private static final String TEST = "src/Test.java";

    public static void main(final String[] args) {
        System.out.println(getClassSimpleName(TEST));
    }

    static String getClassSimpleName(String pathToClassFile) {
        int beginIndex = pathToClassFile.lastIndexOf('/');
        assert beginIndex != -1 : "Missing '/' in path";
        int endIndex = pathToClassFile.indexOf('.', beginIndex);
        assert endIndex != -1 : "Missing '.' in path";
        return pathToClassFile.substring(beginIndex + 1, endIndex);
    }
}