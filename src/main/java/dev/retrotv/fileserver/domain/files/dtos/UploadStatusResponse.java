package dev.retrotv.fileserver.domain.files.dtos;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import dev.retrotv.fileserver.enums.StatusCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UploadStatusResponse implements Serializable {
    @NonNull private UUID sessionId;
    @NonNull private String status;
    @NonNull private String fileName;
    private int progress;
    private int uploadedChunks;
    private int totalChunks;
    private Set<Integer> missingChunks;
    private long fileSize;
    private long lastActivity;

    public void setStatus(@NonNull String status) {
        if (!StatusCode.contains(status)) {
            throw new IllegalArgumentException("유효하지 않은 상태 코드입니다.");
        }

        this.status = status;
    }
}
