package dev.retrotv.fileserver.domain.files.dtos;

import java.io.Serializable;

import dev.retrotv.fileserver.enums.StatusCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChunkUploadResponse implements Serializable {
    @NonNull private String status;
    @NonNull private String message;
    private boolean success;
    private int chunkIndex;
    private int progress;
    private int uploadedChunks;
    private int totalChunks;
    private boolean isComplete;

    public void setStatus(@NonNull String status) {
        if (!StatusCode.contains(status)) {
            throw new IllegalArgumentException("유효하지 않은 상태 코드입니다.");
        }

        this.status = status;
    }
}
