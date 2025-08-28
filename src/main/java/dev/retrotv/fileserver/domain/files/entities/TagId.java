package dev.retrotv.fileserver.domain.files.entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
public class TagId implements Serializable {

    @Column(name = "FILE_ID", length = 36)
    private String fileId;

    @Column(name = "KEY", length = 128)
    private String key;

    public TagId() {}

    public TagId(String fileId, String key) {
        this.fileId = fileId;
        this.key = key;
    }
}
