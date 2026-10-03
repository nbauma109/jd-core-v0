package jd.core.test;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/** Shapes whose line numbers used to shift everything after them. */
public class LineNumberAlignment {
    enum Kind {
        FIRST(1,
                2),
        SECOND(3,
                4);

        final int x;
        final int y;

        Kind(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static int tryWithResourcesAndReturns(Reader first, Reader second) throws IOException {
        try (Reader reader = first) {
            while (true) {
                int c = reader.read();
                if (c < 0) {
                    return second.read();
                }
                if (c == 7) {
                    return -1;
                }
            }
        }
    }

    static String lambdaOnTheCallLine(List<String> list) {
        return list.stream().filter(s -> s.length() > 1).findFirst()
                .orElseThrow(() -> new IllegalStateException("none"));
    }

    static int after() {
        return 42;
    }
}
