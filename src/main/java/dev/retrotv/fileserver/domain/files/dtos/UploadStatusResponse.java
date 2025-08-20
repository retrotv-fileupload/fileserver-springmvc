package dev.retrotv.fileserver.domain.files.dtos;

import java.util.Set;

import dev.retrotv.fileserver.enums.StatusCode;
import lombok.NonNull;

public class UploadStatusResponse {
    @NonNull String sessionId;
    @NonNull String status;
    @NonNull String fileName;
    int progress;
    int uploadedChunks;
    int totalChunks;
    Set<Integer> missingChunks;
    long fileSize;
    long lastActivity;

    public void setStatus(@NonNull String status) {
        if (!StatusCode.contains(status)) {
            throw new IllegalArgumentException("유효하지 않은 상태 코드입니다.");
        }

        this.status = status;
    }
}
