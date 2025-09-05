package dev.retrotv.fileserver.common.exception;

public class HashingFailException extends RuntimeException {
    public HashingFailException(String message) {
        super(message);
    }

    public HashingFailException(String message, Throwable cause) {
        super(message, cause);
    }    
}
