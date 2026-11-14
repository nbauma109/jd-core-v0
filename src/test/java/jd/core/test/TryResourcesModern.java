package jd.core.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

/**
 * Try-with-resources compiled by javac 11+, which does not emit the null checks
 * and the "primary exception" variable of javac 7/8.
 */
public class TryResourcesModern {

    public static long consume(InputStream in) throws IOException {
        long total = 0;
        byte[] buffer = new byte[1024];
        int n;
        while ((n = in.read(buffer)) != -1) {
            total += n;
        }
        return total;
    }

    public static Checksum checksum(Path path, Checksum checksum) throws IOException {
        try (InputStream in = new CheckedInputStream(Files.newInputStream(path), checksum)) {
            consume(in);
        }
        return checksum;
    }

    public static int first(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return in.read();
        }
    }

    public static void copy(Path source, Path target) throws IOException {
        try (InputStream in = Files.newInputStream(source); OutputStream out = Files.newOutputStream(target)) {
            out.write(in.read());
        }
    }

    public static void throwing(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            throw new IllegalStateException("never completes normally");
        }
    }

    public static void endless(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            while (true) {
                in.read();
            }
        }
    }
}
