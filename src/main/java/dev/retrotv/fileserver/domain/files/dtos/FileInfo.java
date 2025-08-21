package dev.retrotv.fileserver.domain.files.dtos;

import java.io.Serializable;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FileInfo implements Serializable {
    @NonNull UUID id;
    @NonNull String fileName;
    long fileSize;
    String mimeType;
    String uploadDate;
    String downloadUrl;
}
