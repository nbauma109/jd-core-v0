package jd.core.test.fallbacks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/** The lambda is analyzed from the enclosing method, then again from the class. */
public class LambdaTryWithResources {
    static Runnable inLambda(File file) {
        return () -> {
            try (InputStream in = new FileInputStream(file)) {
                in.read();
            } catch (IOException e) {
                System.out.println(e);
            }
        };
    }
}
