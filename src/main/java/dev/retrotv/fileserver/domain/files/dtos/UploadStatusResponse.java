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
public class UploadStatusResponse implements Serializable {
    @NonNull private UUID sessionId;
    @NonNull private StatusCode status;
    @NonNull private String fileName;
    private double progress;
    private int uploadedChunks;
    private int totalChunks;
    private Set<Integer> missingChunks;
    private long fileSize;
    private LocalDateTime lastActivity;
}
