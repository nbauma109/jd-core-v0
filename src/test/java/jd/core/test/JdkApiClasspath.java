package jd.core.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** Supplies the requested release's API signatures to the standalone JDT parser. */
final class JdkApiClasspath {
    private static final Map<Integer, String> RELEASES = new HashMap<>();

    private JdkApiClasspath() {
    }

    static synchronized String forRelease(String release) throws IOException {
        int version = Integer.parseInt(release.startsWith("1.") ? release.substring(2) : release);
        if (version >= Runtime.version().feature()) {
            return null;
        }
        String cached = RELEASES.get(version);
        if (cached != null) {
            return cached;
        }
        Path output = Files.createTempFile(Path.of("target"), "jdk-api-" + version + "-", ".jar");
        char releaseCode = Character.toUpperCase(Character.forDigit(version, 36));
        var classes = new HashSet<String>();
        try (var symbols = new ZipFile(Path.of(System.getProperty("java.home"), "lib", "ct.sym").toFile());
                var archive = new ZipOutputStream(Files.newOutputStream(output))) {
            var entries = symbols.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String[] segments = entry.getName().split("/", 3);
                if (segments.length == 3 && segments[0].indexOf(releaseCode) >= 0
                        && segments[2].endsWith(".sig") && !segments[2].equals("module-info.sig")) {
                    String name = segments[2].substring(0, segments[2].length() - 4) + ".class";
                    if (classes.add(name)) {
                        archive.putNextEntry(new ZipEntry(name));
                        try (var input = symbols.getInputStream(entry)) {
                            input.transferTo(archive);
                        }
                        archive.closeEntry();
                    }
                }
            }
        }
        if (classes.isEmpty()) {
            throw new IOException("No JDK API signatures for release " + release);
        }
        RELEASES.put(version, output.toString());
        return output.toString();
    }
}
