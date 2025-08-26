package dev.retrotv.fileserver.domain.files.entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
public class MetadataId implements Serializable {

    @Column(name = "FILE_ID", length = 36)
    private String fileId;

    @Column(name = "META_KEY", length = 128)
    private String metaKey;

    public MetadataId() {}
    public MetadataId(String fileId, String metaKey) {
        this.fileId = fileId;
        this.metaKey = metaKey;
    }
}
