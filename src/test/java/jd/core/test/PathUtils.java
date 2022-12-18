package jd.core.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public class PathUtils {
    public static boolean fileContentEquals(Path path1, Path path2, OpenOption[] openOptions) throws IOException {
        try (InputStream inputStream1 = Files.newInputStream(path1, openOptions);
            InputStream inputStream2 = Files.newInputStream(path2, openOptions)) {
            return contentEquals(inputStream1, inputStream2);
        }
    }

    native static boolean contentEquals(InputStream inputStream1, InputStream inputStream2);
}
