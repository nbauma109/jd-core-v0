package jd.core.test;

public class ForEachReconstruct {
    void iterate() {
        String[] strings;
        int j = (strings = new String[] { "a", "b" }).length; for (int i = 0; i < j; ++i) { String s = strings[i]; System.out.println(s);
        }
    }

    public static void main(String[] args) {
        Object[] localObject = args; int guiMap = 0; for (int localGUIMap1 = localObject.length; guiMap < localGUIMap1; ++guiMap) { Object arg = localObject[guiMap];
            System.out.println(arg);
        }
    }
}
