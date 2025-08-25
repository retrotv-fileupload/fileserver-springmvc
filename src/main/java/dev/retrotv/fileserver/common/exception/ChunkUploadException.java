package dev.retrotv.fileserver.common.exception;

public class ChunkUploadException extends RuntimeException {
    public ChunkUploadException(String message) {
        super(message);
    }

    public ChunkUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
