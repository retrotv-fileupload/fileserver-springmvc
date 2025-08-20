package dev.retrotv.fileserver.domain.files.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InitData {
    @NonNull String fileName;
    long fileSize;
    int totalChunks;
    String mimeType;
}
