package dev.retrotv.fileserver.domain.files.dtos;

import java.util.Set;

import dev.retrotv.fileserver.enums.StatusCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UploadSession {
    @NonNull String sessionId;
    @NonNull String fileName;
    @NonNull String status;
    long fileSize;
    int totalChunks;
    String mimeType;
    String tempDir;
    Set<Integer> uploadedChunks;
    long createdAt;
    long lastActivity;

    public void setStatus(@NonNull String status) {
        if (!StatusCode.contains(status)) {
            throw new IllegalArgumentException("유효하지 않은 상태 코드입니다.");
        }

        this.status = status;
    }
}
