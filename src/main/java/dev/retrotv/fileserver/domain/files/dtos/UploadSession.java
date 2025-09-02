package dev.retrotv.fileserver.domain.files.dtos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
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
    @NonNull private StatusCode status;
    private long fileSize;
    private int totalChunks;
    private String mimeType;
    private String subDir;
    private List<Tag> tags;
    private Set<Integer> uploadedChunks;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivity;
}
