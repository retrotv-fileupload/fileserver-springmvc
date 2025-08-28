package dev.retrotv.fileserver.common.exception;

public class ChunkSaveException extends RuntimeException {
    public ChunkSaveException(String message) {
        super(message);
    }

    public ChunkSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
