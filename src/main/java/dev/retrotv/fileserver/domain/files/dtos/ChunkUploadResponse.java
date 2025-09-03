package dev.retrotv.fileserver.domain.files.dtos;

import java.io.Serializable;

import dev.retrotv.fileserver.enums.StatusCode;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ChunkUploadResponse implements Serializable {
    @NonNull private StatusCode status;
    @NonNull private String message;
    private boolean success;
    private int chunkIndex;
    private int progress;
    private int uploadedChunks;
    private int totalChunks;
    private boolean isComplete;
}
