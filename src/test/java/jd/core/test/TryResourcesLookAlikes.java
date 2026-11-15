package jd.core.test;

import java.io.ByteArrayInputStream;
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

    /** An early return leaves the resource open: only the exceptional path closes it, the body ends with a throw. */
    static void earlyReturn(InputStream in, boolean stop) throws Exception {
        InputStream resource = in;
        try {
            if (stop) {
                return;
            }
            throw new Exception("never completes normally");
        } catch (Throwable t) {
            try {
                resource.close();
            } catch (Throwable suppressed) {
                t.addSuppressed(suppressed);
            }
            throw t;
        }
    }

    static class NotCloseable {
        void close() throws Exception {
            // Empty on purpose: the class has a close() method without being an AutoCloseable
        }
    }

    /** The class has a close() method but is not an AutoCloseable: it cannot be a resource. */
    static void notAutoCloseable(NotCloseable input) throws Exception {
        NotCloseable resource = input;
        try {
            throw new Exception("never completes normally");
        } catch (Throwable t) {
            try {
                resource.close();
            } catch (Throwable suppressed) {
                t.addSuppressed(suppressed);
            }
            throw t;
        }
    }

    /** The variable is assigned in the body: it cannot be the variable of a resource specification. */
    static void reassigned(InputStream input, InputStream other) throws Exception {
        InputStream resource = input;
        try {
            resource = other;
            throw new Exception("never completes normally");
        } catch (Throwable t) {
            try {
                resource.close();
            } catch (Throwable suppressed) {
                t.addSuppressed(suppressed);
            }
            throw t;
        }
    }

    /** The variable is used out of the inner try statement, in the outer catch: it cannot be a resource. */
    static InputStream usedInOuterCatch(InputStream input) throws Exception {
        InputStream resource = input;
        try {
            try {
                throw new Exception("never completes normally");
            } catch (Throwable t) {
                try {
                    resource.close();
                } catch (Throwable suppressed) {
                    t.addSuppressed(suppressed);
                }
                throw t;
            }
        } catch (IOException e) {
            return resource;
        }
    }

    /** The resource can be null and is closed without null check: it is not the same as a resource specification. */
    static void unguardedClose(InputStream input, List<Integer> out) throws Exception {
        InputStream resource = input;
        try {
            out.add(resource.read());
        } catch (Throwable t) {
            try {
                resource.close();
            } catch (Throwable suppressed) {
                t.addSuppressed(suppressed);
            }
            throw t;
        }
        resource.close();
    }

    /** The exception is rethrown only if the close fails: it is swallowed when the close succeeds. */
    static void swallowedWhenClosed() throws Exception {
        InputStream resource = new ByteArrayInputStream(new byte[0]);
        try {
            throw new Exception("swallowed by the cleanup");
        } catch (Throwable t) {
            try {
                resource.close();
            } catch (Throwable suppressed) {
                t.addSuppressed(suppressed);
                throw t;
            }
        }
    }
}
