package dev.retrotv.fileserver.common.stream;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

public class GzipServletOutputStream extends ServletOutputStream {

    private final GZIPOutputStream gzipOutputStream;

    public GzipServletOutputStream(GZIPOutputStream gzipOutputStream) {
        this.gzipOutputStream = gzipOutputStream;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
    }

    @Override
    public void write(int b) throws IOException {
        this.gzipOutputStream.write(b);
    }

}
