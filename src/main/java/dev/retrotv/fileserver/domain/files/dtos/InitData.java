package dev.retrotv.fileserver.domain.files.dtos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class InitData implements Serializable {
    public InitData() {
        this.tags = new ArrayList<>();
        tags.add(new Tag("default","default"));
    }

    @NonNull private String fileName;
    private long fileSize;
    private int totalChunks;
    private String subDir;
    private String mimeType;
    private List<Tag> tags;
}
