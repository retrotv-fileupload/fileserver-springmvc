package dev.retrotv.fileserver.domain.files.dtos;

import java.io.Serializable;
import java.util.List;

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
public class InitData implements Serializable {
    @NonNull private String fileName;
    private long fileSize;
    private int totalChunks;
    private String subDir;
    private String mimeType;
    private List<Tag> tags;
}
