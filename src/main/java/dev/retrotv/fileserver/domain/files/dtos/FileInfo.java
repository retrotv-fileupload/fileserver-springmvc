package dev.retrotv.fileserver.domain.files.dtos;

import io.micrometer.common.lang.NonNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FileInfo {
    @NonNull String id;
    @NonNull String fileName;
    long fileSize;
    String mimeType;
    String uploadDate;
    String downloadUrl;
}
