package dev.retrotv.fileserver.domain.files.dtos;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class DownloadFileInfo {
    private String originalFileName;
    private String fileName;
    private String filePath;
    private String mimeType;
    private Long size;
    private String hash;
}
