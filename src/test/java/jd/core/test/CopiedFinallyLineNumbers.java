package jd.core.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CopiedFinallyLineNumbers {
    static byte[] read(InputStream input, boolean close) throws IOException {
        try {
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] data = new byte[4096];
                int reads = 0;
                int count;
                while ((count = input.read(data, 0, data.length)) != -1) {
                    output.write(data, 0, count);
                    reads++;
                }
                output.flush();
                if (reads == 1) {
                    return data;
                }
                return output.toByteArray();
            }
        } finally {
            if (close) {
                input.close();
            }
        }
    }

    static int after() {
        return 42;
    }
}
