package jd.core.test;

public class ForEachReconstruct {

    /*
     * SUN 1.5 pattern
     */
    void forEachInCatch() {
        try {
            System.out.println("try");
        } catch (Exception e) {
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                System.out.println(stackTraceElement);
            }
        }
    }

    /*
     * IBM pattern
     */
    public static void main(String[] args) {
        Object[] localObject = args; int guiMap = 0; for (int localGUIMap1 = localObject.length; guiMap < localGUIMap1; ++guiMap) { Object arg = localObject[guiMap];
            System.out.println(arg);
        }
    }
}
