package jd.core.test.fallbacks;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/** ECJ keeps the exception thrown by a try-with-resources statement in a variable shared by all its resources. */
public class EcjTryWithResources {
    static void single(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            in.read();
        }
        System.out.println();
    }

    static int returned(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            return in.read();
        }
    }

    static void three(File file) throws IOException {
        try (InputStream first = new FileInputStream(file);
                InputStream second = new BufferedInputStream(first);
                InputStream third = new BufferedInputStream(second)) {
            third.read();
        }
        System.out.println();
    }

    static void caught(File file) {
        try (FileOutputStream out = new FileOutputStream(file); OutputStream buffered = out) {
            buffered.flush();
        } catch (IOException e) {
            System.out.println(e);
        }
        System.out.println();
    }

    static void withFinally(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            in.read();
        } finally {
            System.out.println();
        }
    }

    static int exitsOfTheBody(File file, List<String> names) throws IOException {
        for (String name : names) {
            try (InputStream in = new FileInputStream(name)) {
                if (in.read() < 0) {
                    continue;
                }
                if (in.read() == 0) {
                    break;
                }
                if (in.read() == 1) {
                    return 1;
                }
                System.out.println(name);
            }
        }
        return 0;
    }

    static void sequentialAndNested(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            try (InputStream nested = new FileInputStream(file)) {
                nested.read();
            }
            in.read();
        }
        try (InputStream in = new FileInputStream(file)) {
            in.read();
        }
    }

    static void conditionAtTheEnd(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            if (in.read() > 0) {
                System.out.println();
            }
        }
    }

    static void closedByTheBody(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            in.close();
        }
    }

    static void thrown(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            throw new IOException(String.valueOf(in.read()));
        }
    }

    static int returnedFromNested(File file) throws IOException {
        try (InputStream outer = new FileInputStream(file)) {
            try (InputStream inner = new BufferedInputStream(outer)) {
                if (inner.read() > 0) {
                    return outer.read();
                }
            }
            System.out.println();
        }
        return -1;
    }

    static void inSwitch(File file, int mode) {
        for (int i = 0; i < mode; i++) {
            switch (i) {
                case 0:
                    try (InputStream in = new FileInputStream(file)) {
                        in.read();
                    } catch (IOException e) {
                        System.out.println(e);
                    }
                    break;
                default:
                    System.out.println(i);
            }
        }
    }

    Object snapshot;

    List<String> loopFollowedByAssignment(File file) throws IOException {
        Object name = file.getName();
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        this.snapshot = name;
        return lines;
    }

    Runnable inLambda(File file) {
        return () -> {
            try (InputStream in = new FileInputStream(file)) {
                in.read();
            } catch (IOException e) {
                System.out.println(e);
            }
        };
    }
}
