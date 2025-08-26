package dev.retrotv.fileserver.common.exception;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException() {
        super("존재하지 않는 파일 업로드 세션입니다.");
    }

    public SessionNotFoundException(String message) {
        super(message);
    }

    public SessionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
