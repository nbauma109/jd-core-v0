package jd.core.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Genuine try-with-resources whose compiled form has no closing code after the protected region, because the body
 * never completes normally, or because the resource is known to be null: javac only closes in the handler.
 */
public class TryResourcesWithoutCloseAfterBody {

    static void nullResource() throws IOException {
        try (InputStream in = null) {
            System.out.println(in);
        }
    }

    static void throwingBody(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            throw new IllegalStateException("never completes normally");
        }
    }

    static void infiniteBody(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            while (true) {
                in.read();
            }
        }
    }
}
