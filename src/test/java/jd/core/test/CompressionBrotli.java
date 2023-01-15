package jd.core.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.sourceforge.plantuml.FileUtils;
import net.sourceforge.plantuml.brotli.BrotliInputStream;
import net.sourceforge.plantuml.code.ByteArray;
import net.sourceforge.plantuml.code.Compression;
import net.sourceforge.plantuml.code.NoPlantumlCompressionException;
import net.sourceforge.plantuml.log.Logme;

public class CompressionBrotli implements Compression {

    public byte[] compress(byte[] in) {
        throw new UnsupportedOperationException();
    }

    public ByteArray decompress(byte[] in) throws NoPlantumlCompressionException {
        try (BrotliInputStream brotli = new BrotliInputStream(new ByteArrayInputStream(in));
                ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            FileUtils.copyToStream(brotli, result);
            return ByteArray.from(result.toByteArray());
        } catch (IOException e) {
            Logme.error(e);
            throw new NoPlantumlCompressionException(e);
        }
    }
}
