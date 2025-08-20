package dev.retrotv.fileserver.domain.files.dtos;

import dev.retrotv.fileserver.enums.StatusCode;
import io.micrometer.common.lang.NonNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChunkUploadResponse {
    @NonNull String status;
    String message;
    boolean success;
    int chunkIndex;
    int progress;
    int uploadedChunks;
    int totalChunks;
    boolean isComplete;

    public void setStatus(@NonNull String status) {
        if (!StatusCode.contains(status)) {
            throw new IllegalArgumentException("유효하지 않은 상태 코드입니다.");
        }

        this.status = status;
    }
}
