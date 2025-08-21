package dev.retrotv.fileserver.domain.files.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import dev.retrotv.fileserver.enums.StatusCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadSession implements Serializable {
    @NonNull private UUID sessionId;
    @NonNull private String fileName;
    @NonNull private String status;
    private long fileSize;
    private int totalChunks;
    private String mimeType;
    private String subDir;
    private Set<Integer> uploadedChunks;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivity;

    public void setStatus(@NonNull String status) {
        if (!StatusCode.contains(status)) {
            throw new IllegalArgumentException("유효하지 않은 상태 코드입니다.");
        }

        this.status = status;
    }
}
