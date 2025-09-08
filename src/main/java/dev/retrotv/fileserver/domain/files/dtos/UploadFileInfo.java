package dev.retrotv.fileserver.domain.files.dtos;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

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
public class UploadFileInfo implements Serializable {
    @NonNull UUID id;
    @NonNull String fileName;
    long fileSize;
    String mimeType;
    List<Tag> tags;
}
