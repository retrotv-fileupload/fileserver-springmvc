package dev.retrotv.fileserver.common.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import dev.retrotv.fileserver.common.exception.ChunkMergeException;
import dev.retrotv.fileserver.common.exception.ChunkSaveException;
import dev.retrotv.fileserver.common.exception.FileDownloadException;
import dev.retrotv.fileserver.common.exception.SessionNotFoundException;
import dev.retrotv.framework.foundation.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(FileDownloadException.class)
    public ResponseEntity<ErrorResponse> handleFileDownload(FileDownloadException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(404).body(
            new ErrorResponse("요청하신 파일이 존재하지 않습니다.", NOT_FOUND)
        );
    }

    @ExceptionHandler(ChunkMergeException.class)
    public ResponseEntity<ErrorResponse> handleChunkMerge(ChunkMergeException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(
            new ErrorResponse("파일 업로드 도중 오류가 발생했습니다. 문제가 지속적으로 발생할 경우, 관리자에게 문의하십시오.", BAD_REQUEST)
        );
    }

    @ExceptionHandler(ChunkSaveException.class)
    public ResponseEntity<ErrorResponse> handleChunkSave(ChunkSaveException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(
            new ErrorResponse("파일 업로드 도중 오류가 발생했습니다. 문제가 지속적으로 발생할 경우, 관리자에게 문의하십시오.", BAD_REQUEST)
        );
    }

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSessionNotFound(SessionNotFoundException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(
            new ErrorResponse("요청한 파일 업로드 세션이 존재하지 않습니다.", BAD_REQUEST)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(500).body(
            new ErrorResponse("서버 오류가 발생했습니다.", INTERNAL_SERVER_ERROR)
        );
    }
}
