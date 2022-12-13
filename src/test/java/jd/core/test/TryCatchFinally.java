package jd.core.test;

import java.io.File;
import java.util.Scanner;

public class TryCatchFinally {

    void tryCatchFinally(File file) {
        Scanner sc = null;
        try {
            sc = new Scanner(file);
            while (sc.hasNextLine()) {
                System.out.println(sc.nextLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sc != null) {
                sc.close();
            }
        }
    }
}
