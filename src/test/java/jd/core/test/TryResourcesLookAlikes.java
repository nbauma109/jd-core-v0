package jd.core.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Code that looks like the closing code that the compilers generate for a try-with-resources, without being one:
 * it must stay a try/catch. Each method has a second, unrelated try/catch: the exception table of the method has
 * several entries.
 */
public class TryResourcesLookAlikes {

    static void noSuppressed(InputStream in) throws IOException {
        try {
            in.read();
        } catch (Throwable t) {
            in.close();
            throw t;
        }
        try {
            in.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void swappedSuppressed(InputStream in) throws IOException {
        try {
            in.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.read();
        } catch (Throwable t) {
            try {
                in.close();
            } catch (Throwable suppressed) {
                suppressed.addSuppressed(t);
            }
            throw t;
        }
    }

    static void wrapped(InputStream in) throws IOException {
        try {
            in.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.read();
        } catch (Throwable t) {
            try {
                in.close();
            } catch (Throwable suppressed) {
                t.addSuppressed(suppressed);
            }
            throw new IllegalStateException(t);
        }
    }

    static void otherResource(InputStream in, InputStream other) throws IOException {
        try {
            in.read();
        } catch (Throwable t) {
            try {
                other.close();
            } catch (Throwable suppressed) {
                t.addSuppressed(suppressed);
            }
            throw t;
        }
        try {
            in.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void notJustAfterStore(InputStream in, List<Integer> out) throws IOException {
        try {
            in.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream stream = in;
        out.add(1);
        try {
            out.add(stream.read());
        } catch (Throwable t) {
            try {
                stream.close();
            } catch (Throwable suppressed) {
                t.addSuppressed(suppressed);
            }
            throw t;
        }
    }

    static void notACloseCall(InputStream in) throws IOException {
        try {
            in.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.read();
        } catch (Throwable t) {
            try {
                in.available();
            } catch (Throwable suppressed) {
                t.addSuppressed(suppressed);
            }
            throw t;
        }
    }

    static void manualClose(InputStream in, List<Integer> out) throws IOException {
        try {
            out.add(in.read());
            in.close();
        } catch (Throwable t) {
            try {
                in.close();
            } catch (Throwable suppressed) {
                t.addSuppressed(suppressed);
            }
            throw t;
        }
    }
}
